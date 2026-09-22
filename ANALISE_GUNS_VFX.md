# Análise — Armas, VFX (muzzle flash) e animações não funcionam

**Branch:** `arena/01a0c85a-irons-artifice` — fix `e9e2d90` ✅ CI `35709399649`
**Sintoma relatado:** "The guns and vfx like the muzzle flash, together with the animations do not work"
**Alvo:** NeoForge 21.1.251 / MC 1.21.1 / GeckoLib 4.9.3 (jar `irons_artifice-1.21.1-1.0.0.jar`)

> **TL;DR:** duas quebras específicas do downport 1.21.1, ambas em **assets**, não em código:
> 1. Os 7 itens de arma eram os **únicos itens sem `assets/irons_artifice/models/item/<id>.json`**. Os arquivos `assets/irons_artifice/items/<id>.json` usam o formato `"type": "minecraft:special"` que **só existe a partir do 1.21.4**; no 1.21.1 eles são ignorados, o item cai no *missing model* (não é custom renderer) e **nunca chega no GeckoLib**. Resultado: arma invisível, sem modelo, sem animação, sem mãos em primeira pessoa, sem attachments e sem emissão de muzzle flash (tudo isso mora dentro do `GunInHandRenderer`).
> 2. `assets/minecraft/atlases/particles.json` usava o sprite source `neoforge:directory_paletted_permutations`, que **não existe no NeoForge 21.1**, então a definição era descartada e os sprites `_fire` / `_tinted_masked` / `_white_mask` **nunca eram costurados no atlas** de partículas.

---

## 1. Por que a arma não renderizava (causa raiz #1)

### 1.1 Caminho de render no 1.21.1

Patch do NeoForge para `ItemRenderer` (1.21.1):

```java
if (!model.isCustomRenderer() && (!stack.is(Items.TRIDENT) || flag)) {
    ... renderiza quads normais ...
} else {
    IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(...);   // Neo: BEWLR
}
```

`IClientItemExtensions#getCustomRenderer()` (default) devolve `Minecraft.getInstance().getItemRenderer().getBlockEntityRenderer()` — e o `BlockEntityWithoutLevelRenderer` é exatamente a classe em que o GeckoLib injeta:

```java
@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixin {
    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    public void geckolib$renderGeckolibItem(...) {
        final BlockEntityWithoutLevelRenderer geckolibRenderer = GeoRenderProvider.of(stack).getGeoItemRenderer();
        if (geckolibRenderer != null) { geckolibRenderer.renderByItem(...); ci.cancel(); }
    }
}
```

Ou seja: **para o GeckoLib renderizar um item no 1.21.1, o item precisa ter um modelo com `"parent": "builtin/entity"`** (`BakedModel#isCustomRenderer() == true`). Sem isso, o `GunInHandRenderer` nunca é chamado.

### 1.2 O que tinha no disco

| Caminho | Status no 1.21.1 |
|---|---|
| `assets/irons_artifice/items/flintlock.json` → `{"model": {"type": "minecraft:special", "base": "irons_artifice:item/gun_display", ...}}` | **Ignorado.** Formato `items/<id>.json` existe só no 1.21.4+ |
| `assets/irons_artifice/models/item/flintlock.json` | **Não existia** (nem para `musket`, `blunderbuss`, `arquebus`, `clockwork_rifle`, `blackpowder_revolver`, `six_shooter`) |
| `assets/irons_artifice/models/item/gun_display.json` | OK — `"parent": "builtin/entity"` + transforms |
| `assets/irons_artifice/models/item/pistol_display.json` | OK — filho de `item/gun_display` |

Verificação: dos 39 itens registrados, os **únicos 7 sem modelo eram exatamente as 7 armas** (script comparando `ITEMS.registerItem("...")` × `models/item/*.json`).

No 1.21.1 o modelo do item é resolvido por `ItemModelShaper` em `models/item/<registry_name>.json`. Arquivo ausente ⇒ *missing model* ⇒ `isCustomRenderer() == false` ⇒ caminho vanilla ⇒ item **invisível** (o missing model não tem quads) e todo o pipeline do GeckoLib morto.

O upstream (26.x) também não tem `models/item/<arma>.json`: lá o `items/<arma>.json` + `minecraft:special` **é** o mecanismo (o `"base": "irons_artifice:item/gun_display"` faz o papel do `builtin/entity`). No 1.21.1 o equivalente é o modelo com `parent` apontando para os display models — que é exatamente o que o datagen portado já gera (`ItemModelDataGenerator` → `withExistingParent(id, displayParent)`), mas **`src/generated/resources` estava desatualizado** (nunca foi rodado `runData` depois do downport; o CI só roda `./gradlew build`).

### 1.3 Fix

Criados os 7 arquivos (saída idêntica à do datagen):

```text
src/generated/resources/assets/irons_artifice/models/item/flintlock.json
                                                      /musket.json
                                                      /blunderbuss.json
                                                      /arquebus.json
                                                      /clockwork_rifle.json
                                                      /blackpowder_revolver.json
                                                      /six_shooter.json
```

```json
{ "parent": "irons_artifice:item/gun_display" }
```

(`blackpowder_revolver` e `six_shooter` usam `irons_artifice:item/pistol_display`, igual ao datagen e ao que os `items/*.json` faziam.)

A cadeia `<arma>` → `item/gun_display` → `builtin/entity` é padrão vanilla (ex.: `item/shulker_box` → `item/template_shulker_box` → `builtin/entity`), então o `builtin/entity` também vale como avô.

**Efeito:** modelo 3D, animações `idle`/`fire`/`reload`/`equip`, mãos em primeira pessoa, attachments e a emissão do muzzle flash (feita em `GunInHandRenderer#handleMuzzleFlashEmission`, no osso `attachment_muzzle`) voltam a funcionar.

---

## 2. Por que o muzzle flash não aparecia (causa raiz #2)

`src/main/resources/assets/minecraft/atlases/particles.json` (como veio do upstream):

```json
{ "sources": [ { "type": "neoforge:directory_paletted_permutations",
                 "textures": "muzzle_flash/particle",
                 "palette_key": "irons_artifice:muzzle_flash/palettes/tinted",
                 "palettes": "muzzle_flash/palettes" } ] }
```

Esse tipo de sprite source **não existe no NeoForge 21.1** (a classe `DirectoryPalettedPermutations` só aparece em versões mais novas do NeoForge; varredura de árvore em `1.21.1`, `1.21.8` e `1.21.11` só encontra `PalettedContainer.java.patch`).

E o que acontece quando o tipo é desconhecido? O `SpriteSourceList#load` do 1.21.1 faz:

```java
for (Resource resource : resourceManager.getResourceStack(resourcelocation)) {   // nota: getResourceStack = MERGE de todos os packs
    try { list.addAll(SpriteSources.FILE_CODEC.parse(dynamic).getOrThrow()); }
    catch (Exception e) { LOGGER.error("Failed to parse atlas definition {} in pack {}", ...); }  // e segue sem esse source
}
```

⇒ a definição é **descartada com log**, sem crash, e os sprites `..._fire`, `..._tinted_masked`, `..._white_mask` (13 texturas × 3 sufixos = 39 sprites) nunca entram no atlas `minecraft:particles`. O `MuzzleFlashParticle`:

* pega `_fire` via `SpriteSet` (`assets/irons_artifice/particles/*.json`);
* reescreve para `_tinted_masked` (+ `_white_mask` num segundo passe) quando há tint:
  `atlas.getSprite(fireName.withPath(basePath + "_tinted_masked"))`.

Sprite inexistente ⇒ `MissingTextureAtlasSprite` (quadrado rosa/preto) ou nada visível ⇒ "o VFX não funciona".

### Fix

Trocado pelo tipo **vanilla** que existe no 1.21.1 e produz exatamente os mesmos nomes (`<textura>_<sufixo>`):

```json
{ "sources": [ {
    "type": "minecraft:paletted_permutations",
    "textures": [ "irons_artifice:muzzle_flash/particle/explosion_1", ... 13 bases ... ],
    "palette_key": "irons_artifice:muzzle_flash/palettes/tinted",
    "permutations": {
      "fire": "irons_artifice:muzzle_flash/palettes/fire",
      "tinted_masked": "irons_artifice:muzzle_flash/palettes/tinted_masked",
      "white_mask": "irons_artifice:muzzle_flash/palettes/white_mask"
    } } ] }
```

Checagens feitas antes de aplicar:

* `minecraft:paletted_permutations` é registrado no 1.21.1 (`SpriteSources.PALETTED_PERMUTATIONS`, patch do NeoForge só troca o mapa `TYPES` por `ClientHooks.makeSpriteSourceTypesMap()`, não renomeia registros);
* os 13 PNGs base existem (`textures/muzzle_flash/particle/*.png`);
* cada paleta tem **4 pixels** (4×1, PNG indexado) — o vanilla exige tamanho igual entre `palette_key` e cada permutation, senão loga `Palette mapping has different sizes` e descarta o sprite;
* `palette_key`/`permutations` são resolvidos como `textures/<path>.png` (sem o prefixo `palettes/` no 1.21.1);
* como o atlas é montado com `getResourceStack` (merge de packs), o source vanilla do `particles.json` **continua valendo** (partículas vanilla e `irons_artifice:bullet_trail` de `textures/particle/` não são afetados).

---

## 3. O que NÃO era o problema (descartado)

* Código de render/anim: `GunInHandRenderer`, controllers (`gun_animation_controller` / `Actions` com `idle`/`fire`/`reload`/`equip`), `AnimationAdjuster`s, `MuzzleFlashEmitter`, `ClientHelper`, packets (`ClientboundMuzzleFlashPacket` etc.), registro dos providers de partícula e as poses de braço (`enumextensions`) — tudo confere com a API do GeckoLib 4.9.3 / NeoForge 21.1.
* Nomes de assets: `geo/item/<arma>.geo.json`, `animations/item/<arma>.animation.json` e `textures/item/<arma>.png` existem e casam com o registry name usado por `DefaultedItemGeoModel`.
* Os `assets/irons_artifice/items/*.json` (1.21.4+) continuam no repositório, mas são **inertes** no 1.21.1 — não quebram nada, só não fazem nada.

---

## 4. Como validar

1. Baixar o artifact do CI `35709399649` (jar `irons_artifice-1.21.1-1.0.0.jar`).
2. Em jogo: segurar a arma → deve aparecer o modelo 3D na mão e no inventário (não mais slot vazio); atirar → animação `fire` + muzzle flash em chamas na boca do cano; recarregar (R) → animação `reload`.
3. No log, some o `Failed to parse atlas definition minecraft:atlases/particles.json ... directory_paletted_permutations`; deve continuar **sem** erros novos.
4. Se algo continuar quebrado, mandar o `latest.log` (Prism) junto do horário — o próximo passo de análise depende de qual parte falhou (arma invisível vs. flash ausente vs. não atira).

Se o datagen for rodado (`./gradlew runData`), os 7 modelos de arma são regenerados com o conteúdo idêntico ao deste fix.
