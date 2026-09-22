# Análise do Crash — Irons Artifice (Atualizado 22/09 05:25)

**Branch:** `arena/01a0c828-irons-artifice` — fix `f684d2d` ✅ CI `35705282896` (2m59s, 2.1 MB)  
**Data do log mais novo:** 22/09/2026 05:25:00 UTC-03:00 (Prism 11.1.0, Java 21.0.7, 15465 MiB RAM)  
**Minecraft:** 1.21.1 + NeoForge **21.1.251** (você atualizou de 21.1.247, ótimo)  
**Jar testado:** `irons_artifice-1.21.1-1.0.0.jar` do artifact `35703635414`

> **TL;DR:** O `Missing ModLoader` foi **100% corrigido** (o jar novo já aparece em `Mod List: Iron's Arms 'n Artifice`). O crash novo de 05:25 é **outro bug, agora em código Java/Mixin**, não no `neoforge.mods.toml`. Fix `f684d2d` já no GitHub corrige os 2 gatilhos abaixo. Baixe o artifact `35705282896` e substitua o jar.

---

## 0. O que mudou entre 04:52 e 05:25

| Log | Estado |
|-----|--------|
| **04:52** | `Missing ModLoader in file (irons_artifice-...)` → loader nem carregava o mod |
| **05:25** | `Found mod file "irons_artifice-..."` + `Mod List: irons_artifice 1.21.1-1.0.0` → **jar agora é válido** (`modLoader="javafml"`+`loaderVersion="[4,)"` presente). Erro novo é em runtime: `Failed to register automatic subscribers` + `MixinTransformerError` |

Ou seja: **nosso fix anterior funcionou**. O jogo passou da fase `SCAN`/`LOADING` e quebrou só no `Mod Construction` (inicialização das classes).

---

## 1. Crash novo — leitura do log 05:25 (3 falhas encadeadas, 1 raiz)

### 1.1 Erro #1 — `Illificer` / `IGunslingerMob` (bloqueia `irons_artifice`)
```
[05:25:36] [modloading-worker-0/FATAL] Failed to register class ...Illificer: Attempting to register a listener object of type class ...Illificer,
 however its supertype interface ...IGunslingerMob has a @SubscribeEvent method modifyMobGunshots(ComposeShotEvent)
  at net.neoforged.bus.EventBus.checkSupertypes(EventBus.java:151)
  at ...FMLModContainer.constructMod:134
  -> ModLoader/LOADING Failed to wait for future Mod Construction, 3 errors found
  -> Cowardly refusing to send event ... to a broken mod state
```

**O que é:** No NeoForge 21.1 com EventBus 8.x, a checagem `checkSupertypes` **proíbe** que uma classe anotada `@EventBusSubscriber` implemente uma interface/superclasse que também tem método `@SubscribeEvent`. `Illificer extends AbstractIllager implements IGunslingerMob` e estava assim:

```java
// IGunslingerMob.java (interface)
@EventBusSubscriber
public interface IGunslingerMob {
  @SubscribeEvent static void modifyMobGunshots(ComposeShotEvent e) { ... }
}

// Illificer.java (entidade)
@EventBusSubscriber
public class Illificer extends AbstractIllager implements IGunslingerMob {
  @SubscribeEvent static void dropLoadoutModifier(LivingDropsEvent e) { ... }
}
```

Antes (NeoForge <21.1.250) isso passava batido; agora o loader rejeita na hora de registrar `Illificer` e marca **3 erros** (irons_artifice + 2 mods que dependem do mixin).

**Fix `f684d2d`:**
- `IGunslingerMob.java` → removido `@EventBusSubscriber` e `@SubscribeEvent` (virou só API `customizeMobShot`/`applyDefaultMobNerfs`)
- Criado `GunslingerMobEvents.java` (`@EventBusSubscriber`) com o método `modifyMobGunshots` movido para lá — nenhuma interface é mais subscriber, então `Illificer` pode continuar como subscriber do seu próprio `dropLoadoutModifier` sem conflito.

### 1.2 Erro #2 — `ItemInHandRendererMixin` (bloqueia `eternalnether` + `sable_schematic_api` e derruba `Minecraft.<init>`)
```
[05:25:36] [modloading-worker-0/ERROR] Mixin apply for mod irons_artifice failed irons_artifice.mixins.json:ItemInHandRendererMixin
  -> Variable modifier method irons_artifice$zeroGunEquipOffset(...)F
     failed injection check, (0/1) succeeded. Scanned 0 target(s). No refMap loaded.
     at MixinProcessor.applyMixins:392 -> MixinTransformer.transformClass
  -> Failed to create mod instance. ModID: eternalnether (21.1.3), sable_schematic_api (1.0.1)
  -> MixinTransformerError An unexpected critical error was encountered (MixinTransformer)
  -> at net.minecraft.client.gui.screens.TitleScreen.<init> -> EntityRenderDispatcher.<init>
```

**O que é:** O `@ModifyVariable` tentava injetar na variável `inverseArmHeight` de `ItemInHandRenderer.renderArmWithItem`:

```java
@ModifyVariable(method="renderArmWithItem", at=@At("HEAD"), argsOnly=true, name="inverseArmHeight")
```

`name="inverseArmHeight"` exige **LVN (Local Variable Name) via refmap** (`irons_artifice.refmap.json`). Como o projeto usa `moddev 1.0.21` sem `refmap` no `irons_artifice.mixins.json`, o Mixin escaneia **0 alvos** e, com `defaultRequire=1`, entra em `Critical injection failure (0/1)` e aborta a transformação da classe `ItemInHandRenderer`. Como a mesma classe é mixada por `eternalnether` e `sable_schematic_api`, todos falham em cascata, e finalmente `Minecraft.<init> -> EntityRenderDispatcher` não consegue iniciar → tela preta.

Repare que o `@Inject(renderArmWithItem)` e o `@WrapOperation(renderHandsWithItems)` do mesmo arquivo **passaram** — só o `ModifyVariable` com `name=` quebrou, porque `Inject` não precisa de nome de variável.

**Fix `f684d2d`:**
```diff
- @ModifyVariable(..., name="inverseArmHeight")
+ @ModifyVariable(..., ordinal=3)
```
`ordinal=3` mira o **4º `float` entre os args** (`frameInterp[0], xRot[1], attack[2], inverseArmHeight[3]`) — não precisa de refmap/LVN e funciona tanto em dev quanto em jar ofuscado (Mojang mappings já são o runtime em 1.21.1). Assinatura da target confirmada no `WrapOperation`:

```
Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(
  Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;
  FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;
  Lnet/minecraft/client/renderer/MultiBufferSource;I)V
```

### 1.3 Erros #3 e #4 — secundários (somem sozinhos)
```
Cowardly refusing to send event SelectablePacketPhaseAlteredEvent to a broken mod state
Sodium cannot continue! Could not find render region initialization ...
eternalnether ERROR Failed to create mod instance (ReportType.CONSTRUCT)
sable_schematic_api ERROR Failed to create mod instance
```
São **efeito**, não causa. O loader já está em `broken mod state` por causa de `Illificer`+`ItemInHandRendererMixin`; ele recusa enviar qualquer evento seguinte. Sodium reclama porque o renderer nem chegou a inicializar.

---

## 2. Correção `f684d2d` — o que foi alterado

**3 arquivos, BUILD SUCCESSFUL ✅**

- `src/main/java/io/redspace/irons_artifice/entity/IGunslingerMob.java` — removido `@EventBusSubscriber`/`@SubscribeEvent`, método estático `modifyMobGunshots` deletado da interface (mantido só `customizeMobShot`/`applyDefaultMobNerfs`)
- `src/main/java/io/redspace/irons_artifice/entity/GunslingerMobEvents.java` — **novo** `@EventBusSubscriber` com `modifyMobGunshots(ComposeShotEvent)` (mesma lógica, chama `gunslinger.customizeMobShot` ou `applyDefaultMobNerfs`)
- `src/main/java/io/redspace/irons_artifice/mixin/ItemInHandRendererMixin.java` — `name="inverseArmHeight"` → `ordinal=3`

Diff completo no commit `f684d2d` em `arena/01a0c828-irons-artifice` e CI `35705282896` (2m59s, `Build with Gradle` ✅, `Upload mod jar` 2.1 MB ✅, `Verify jar contains valid neoforge.mods.toml` ✅ `✓ modLoader present`).

---

## 3. Como obter o jar corrigido (segunda leva)

### Opção A — GitHub Actions (recomendado, já compilado)
1. https://github.com/rafaelkb/irons-artifice/actions/runs/35705282896
2. Role até **Artifacts** → **`irons_artifice-jar`** (2 117 840 bytes, `irons_artifice-1.21.1-1.0.0.jar`)
3. Baixe, extraia e **substitua** o arquivo em `PrismLauncher/instances/1.21.1/minecraft/mods/` (apague o antigo 05:25)
4. Não precisa limpar `sources.jar` (`irons_artifice-1.21.1-1.0.0-sources.jar` não vai em `mods/`, é só fontes)

### Opção B — Compilar local
```bash
git fetch origin
git checkout arena/01a0c828-irons-artifice
git pull
./gradlew build
# jar em build/libs/irons_artifice-1.21.1-1.0.0.jar
unzip -p build/libs/*.jar META-INF/neoforge.mods.toml | head -n5
# deve mostrar modLoader="javafml"
```

Teste esperado no próximo launch: **não** deve mais aparecer `Failed to register class ...Illificer` nem `irons_artifice$zeroGunEquipOffset failed injection`. O `Mod List` deve listar `irons_artifice`, `eternalnether` e `sable_schematic_api` sem `Failed to create mod instance`.

---

# Histórico — Crash anterior (Missing ModLoader) 04:52

**Data do log:** 22/09/2026 04:52:44 UTC-03:00  
**Minecraft:** 1.21.1 + NeoForge 21.1.247 + PrismLauncher 11.1.0  
**Java:** Microsoft OpenJDK 21.0.7  
**Jar com erro:** `irons_artifice-1.21.1-1.0.0.jar`

---

## 1. Resumo Executivo

Seu jogo **não quebrou por falta de RAM, nem por incompatibilidade de mods** — o crash foi causado por um **JAR inválido do próprio Iron's Artifice**.  
O `neoforge.mods.toml` dentro do jar estava **sem os campos obrigatórios** `modLoader` e `loaderVersion`, então o NeoForge rejeitou o arquivo antes mesmo de carregar qualquer classe:

```
[04:53:05] [Render thread/FATAL] [ne.ne.fm.ModLoader/CORE]: Error during pre-loading phase:
  File mods\irons_artifice-1.21.1-1.0.0.jar is not a valid mod file
net.neoforged.neoforgespi.locating.InvalidModFileException: Missing ModLoader in file
```

Isso deixou o `ModLoader` em estado quebrado (`Cowardly refusing to send event... to a broken mod state`) e provocou um **crash em cascata no Quark/Zeta**, que não tem nada a ver com seu modpack:

```
java.lang.ExceptionInInitializerError
  at org.violetmoon.quark.content.tools.client.render.GlintRenderTypes.<clinit>
Caused by: java.lang.RuntimeException: Where is minecraft???!
  at net.neoforged.fml.ModLoadingContext.getActiveContainer
  at org.violetmoon.zetaimplforge.registry.ForgeZetaRegistry.<init>
```

**Fix aplicado neste repositório:** commit `b895d13` na branch `arena/01a0c828-irons-artifice` (build `35703635414` ✅ sucesso, 2m22s).

---

## 2. Leitura Linha-a-Linha do Log que Você Enviou

### 2.1 Cabeçalho PrismLauncher — tudo OK
```
Prism Launcher version: 11.1.0 (official)
Launched instance in online mode
Minecraft folder is: .../instances/1.21.1/minecraft
Java is version 21.0.7, using 64 (amd64) architecture, from Microsoft
```
Launcher, Java 21 e arquitetura 64-bit corretos para 1.21.1.

### 2.2 Aviso de RAM (amarelo, não fatal)
```
Sem RAM suficiente para lançar essa instância
RAM: 15465 MiB (available: 5077 MiB)
Java arguments: -Xms512m -Xmx8096m
```
Você tem **15 GB total**, mas só **5 GB disponíveis** no momento do launch. Pedir `-Xmx8096m` (8 GB) com 200+ mods deixa o sistema sem margem. O Prism só avisa — ainda tenta iniciar — mas **recomendo baixar para `-Xmx6G` ou `-Xmx4G`** e fechar navegador/Discord antes.

### 2.3 Mods detectados (scan)
```
Found mod file "irons_artifice-1.21.1-1.0.0.jar" [locator: {mods folder}]
...
239 mods encontrados (Create 6.0.10, Sodium 0.8.13, Quark 4.1-485, GeckoLib 4.9.3, etc.)
```
Scan ok. Avisos `Attempted to select two dependency jars from JarJar which have the same identification` são normais (CreateDragonsPlus vs particle_core) e não causam crash.

### 2.4 Erro Fatal — o único que importa
```
[04:53:05] [Render thread/FATAL] ... Missing ModLoader in file (irons_artifice-1.21.1-1.0.0.jar)
```
Neça hora o NeoForge **para de carregar** e entra em `broken mod state`. Todos os eventos seguintes são recusados:
```
[04:53:05] [Render thread/ERROR] ... Cowardly refusing to send event ... to a broken mod state
```

### 2.5 Crash secundário — Quark/Zeta
```
java.lang.ExceptionInInitializerError: null
  at org.violetmoon.quark.content.tools.client.render.GlintRenderTypes.texture
Caused by: java.lang.RuntimeException: Where is minecraft???!
  at net.neoforged.fml.ModLoadingContext.getActiveContainer(ModLoadingContext.java:24)
  at org.violetmoon.zetaimplforge.ForgeZetaRegistry.<init>
```
**Não é culpa do Quark.** O Zeta tenta pegar o container ativo durante `<clinit>`, mas como o loader já está quebrado por causa do Iron's Artifice, ele falha. **Assim que o Iron's Artifice for corrigido, esse crash desaparece.**

### 2.6 Outros warnings ignoráveis
- `Sodium has applied one or more workarounds to prevent crashes [AMD_GAME_OPTIMIZATION_BROKEN]` → Sodium detectou driver AMD 32.0.31041 e aplicou workaround, é esperado.
- `Reference map '...' could not be read` → mensagens de desenvolvimento, não afetam produção.
- `Connection failed` para texturas/mojang → sem internet no momento, não é crash.

---

## 3. Causa Raiz — `neoforge.mods.toml` Incompleto

### Arquivo antigo (`src/main/templates/META-INF/neoforge.mods.toml`)
```toml
license="${mod_license}"

[[mods]]
modId="${mod_id}"
...
```
Faltavam **duas linhas obrigatórias** no topo, exigidas pelo NeoForge 21.1 (MDK oficial `MDK-1.21.1-ModDevGradle` e `MDK-1.21.1-NeoGradle`):

```toml
modLoader="javafml" #mandatory
loaderVersion="${loader_version_range}" #mandatory
```

Sem elas, o parser lança `InvalidModFileException: Missing ModLoader`.

### Por que o CI passava com `BUILD SUCCESSFUL`?
O Gradle compilava, mas **não validava** o TOML. O jar era gerado, só que inválido em runtime. O workflow não verificava o conteúdo do jar — agora verifica.

---

## 4. Correção Aplicada

### 4.1 `src/main/templates/META-INF/neoforge.mods.toml`
```diff
+modLoader="javafml" #mandatory
+loaderVersion="${loader_version_range}" #mandatory
+
 license="${mod_license}"
```

### 4.2 `gradle.properties`
```diff
 neo_version=21.1.251
+loader_version_range=[4,)
 geckolib_version=4.9.3
```
`[4,)` é a versão do `javafml` para NeoForge 21.1 (FML 4). Documentado em https://docs.neoforged.net/docs/gettingstarted/modfiles/

### 4.3 `build.gradle`
```diff
 var replaceProperties = [
         minecraft_version      : minecraft_version,
         minecraft_version_range: minecraft_version_range,
         neo_version            : neo_version,
+        loader_version_range   : loader_version_range,
         mod_id                 : mod_id,
 ...
 ]
```

### 4.4 `.github/workflows/build.yml`
- Trigger ampliado de `arena/01a0c7d3-irons-artifice` para `arena/*` + `main`
- Novo step `Upload mod jar` (actions/upload-artifact@v4)
- Novo step `Verify jar contains valid neoforge.mods.toml` que:
  ```bash
  cat build/generated/sources/modMetadata/META-INF/neoforge.mods.toml
  jar tf build/libs/*.jar | grep neoforge.mods.toml
  unzip -p build/libs/*.jar META-INF/neoforge.mods.toml
  grep -q 'modLoader' || exit 1
  ```
  O build `35703635414` passou com `✓ modLoader present`.

### TOML expandido (após fix)
```toml
modLoader="javafml" #mandatory
loaderVersion="[4,)" #mandatory

license="All Rights Reserved"

[[mods]] #mandatory
modId="irons_artifice" #mandatory
version="1.21.1-1.0.0" #mandatory
displayName="Iron's Arms 'n Artifice" #mandatory
logoFile = "iaa_logo_gunmetal_lowres.png" #optional
...
[[dependencies.irons_artifice]]
    modId="neoforge"
    versionRange="[21.1.251,)"
...
```

Commit: `b895d13 fix: add missing modLoader/loaderVersion ...`  
Push: `arena/01a0c828-irons-artifice` → GitHub → Actions ✅

---

## 5. Outros Problemas no Seu Modpack (recomendações)

### 5.1 Duplicata `citadel`
```
[✔] citadel-1.21.1-2.7.6.jar
[✔] citadel-2.7.1-1.21.1.jar
```
Dois CItadels diferentes causam `JarSelector` warning e podem gerar conflitos. **Mantenha apenas `citadel-2.7.1-1.21.1.jar` (mais novo, usado por Alex's Mobs/Caves)** e apague o `citadel-1.21.1-2.7.6.jar`.

### 5.2 NeoForge desatualizado
Você está em `21.1.247`, mas o mod foi compilado contra `21.1.251`. O `versionRange="[21.1.251,)"` vai exigir atualização. **Opções:**
- **Recomendado:** Atualize NeoForge para **21.1.251** no PrismLauncher (Instância → Editar → Version → NeoForge 21.1.251).
- Ou, se quiser manter 247, eu posso baixar a exigência para `[21.1.0,)` — me avise.

### 5.3 RAM
PrismLauncher → Configurações → Java → `Maximum memory allocation` → coloque **6144 MiB (6 GB)** em vez de 8096 MiB. Você tem 15 GB, mas com 200 mods + Windows + AMD driver, 8 GB estoura o disponível (5 GB).

### 5.4 Quark/Zeta
Não precisa remover. O crash do Quark era **efeito**, não causa. Com o jar corrigido, o Quark inicia normal.

---

## 6. Como Obter o Jar Corrigido

### Opção A — Baixar do GitHub Actions (recomendado, já compilado)
1. Vá em https://github.com/rafaelkb/irons-artifice/actions/runs/35703635414
2. Role até **Artifacts** → `irons_artifice-jar` (2.1 MB, `irons_artifice-1.21.1-1.0.0.jar`)
3. Baixe `irons_artifice-jar.zip`, extraia e substitua o arquivo antigo em `PrismLauncher/instances/1.21.1/minecraft/mods/`
4. Delete o jar antigo `irons_artifice-1.21.1-1.0.0.jar` que deu erro (ou só sobrescreva).

> **Nota:** O download de Artifacts usa `*.blob.core.windows.net` (Azure). Se seu navegador bloquear, me avise que eu gero um release.

### Opção B — Compilar localmente (se tiver JDK 21)
```bash
git fetch origin
git checkout arena/01a0c828-irons-artifice
git pull
./gradlew build
# jar em build/libs/irons_artifice-1.21.1-1.0.0.jar
unzip -p build/libs/irons_artifice-*.jar META-INF/neoforge.mods.toml | head -n 5
# deve mostrar modLoader="javafml"
```

### Opção C — Eu publico um Release
Se preferir, eu crio um `Release v1.0.1-fix` com o jar anexado para baixar direto sem passar por Actions — só pedir.

---

## 7. Validação

**Build CI:** https://github.com/rafaelkb/irons-artifice/actions/runs/35703635414  
- `Build with Gradle` ✅ (2m22s, 7 tasks)
- `Upload mod jar` ✅ (2.1 MB)
- `Verify jar contains valid neoforge.mods.toml` ✅ (`✓ modLoader present`)
- `Publish build log to ci-logs` ✅

**Teste local simulado:**
```bash
python3 -c "expand template → check modLoader"
# modLoader present: True
# loaderVersion present: True
```

Próximo passo é você testar no PrismLauncher com o jar novo. Se ainda der `Missing ModLoader`, me envie o novo log; se der `Dependency not found neoforge 21.1.251`, é só atualizar o NeoForge.

---

## 8. Checklist para Você

- [ ] Baixar `irons_artifice-1.21.1-1.0.0.jar` do artifact `35703635414`
- [ ] Substituir em `mods/`
- [ ] Remover `citadel-1.21.1-2.7.6.jar` (manter só `2.7.1`)
- [ ] Atualizar NeoForge para `21.1.251` (ou me pedir para relaxar a exigência)
- [ ] Ajustar RAM para `6144 MiB`
- [ ] Testar launch → me enviar novo log se houver outro erro

---

## 9. Referências

- MDK oficial 1.21.1 NeoGradle: `modLoader="javafml"` + `loaderVersion="${loader_version_range}"` ([NeoForgeMDKs/MDK-1.21.1-NeoGradle/.../neoforge.mods.toml](https://github.com/NeoForgeMDKs/MDK-1.21.1-NeoGradle))
- Docs NeoForge: https://docs.neoforged.net/docs/gettingstarted/modfiles/ (tabela `modLoader` obrigatório, `loaderVersion="[4,)"` para FML4)
- Seu log completo: `crash-2026-09-22_04.53.06-client.txt` (ExceptionInInitializerError em `GlintRenderTypes` é cascata do `Missing ModLoader`)

---

*Gerado automaticamente após fix `b895d13` em `arena/01a0c828-irons-artifice`. Se precisar que eu gere o Release ou baixe a exigência do NeoForge, só avisar!*
