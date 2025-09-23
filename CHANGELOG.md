# LambdaMCDev changelog

## 1.0.0

- Initial release.
- Added support YALMM mappings.
- Added utilities related to versioning.

## 1.1.0

- Added tasks to generate `fabric.mod.json` and `neoforge.mods.toml` files.
- Added experimental access widener to access transformer converter.
- Tweaked how loom is required to be compatible with loom forks.

## 1.2.0

- Added NeoForge's JarJar metadata generation.

### 1.2.1

- Fixed `fabric.mod.json` generator dependency constraints being too strict.

### 1.2.2

- Added way to specify mixins in `neoforge.mods.toml` file generation.

### 1.2.3

- Release candidates are now qualified as "beta" release type in `ModUtils`.
- Make release-candidate-specific entry optional in changelogs.

### 1.2.4

- Updated to Fabric Loom 1.11.4.

## 1.3.0

- Added utilities to set up Mojmap remapping and artifact publishing.

### 1.3.1

- Fixed `ModUtils.fetchChangelog` link reference regex.

## 1.4.0

- Added tasks to package mods for easy processing by release CIs.

## 1.5.0

- Added more flexible `from(Configuration)` method to `GenerateNeoForgeJiJDataTask`.
- Added more control on NMT dependency data.
- Added Yumi entrypoints shortcuts in NMT.
- Added a way to specify Jar-in-Jar Jars in FMJs.
- Fixed contributors field not being properly copied in `ModBase.copyTo`.

### 1.5.1

- Improved mod manifests copying.
