# Configuration

The following configurations can be set in a `.editorconfig` file to configure preferences for analyzers. Note, that **all** configurations are set to false by default, so you have to turn them on individually according to the below table.

| Property | Possible values | Description |
| --- | --- | --- |
| `natls.style.comparisons` | `sign`, `short`, `false` | [`NL006`](../tools/ruletranslator/src/main/resources/rules/NL006)|
| `natls.style.disallowtoplevelvars` | `true`, `false` | [`NL018`](../tools/ruletranslator/src/main/resources/rules/NL018)|
| `natls.style.qualifyvars` | `true`, `false` | [`NL019`](../tools/ruletranslator/src/main/resources/rules/NL019)|
| `natls.style.discourageindependent` | `true`, `false` | [`NL028`](../tools/ruletranslator/src/main/resources/rules/NL028)|
| `natls.style.disallowgitmarkers` | `true`, `false` | [`NL030`](../tools/ruletranslator/src/main/resources/rules/NL030)|
| `natls.style.discourageinlineparameters` | `true`, `false` | [`NL031`](../tools/ruletranslator/src/main/resources/rules/NL031)|

# Example

```editorconfig
[*]
natls.style.comparisons=sign
[**/*.NSC]
natls.style.comparisons=short
```
