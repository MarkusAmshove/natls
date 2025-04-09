# Language Server Configuration

The following configurations can be set in the language client to change behavior of the language server.

| Property                                    | Possible values | Default value | Explanation                                                                 |
|---------------------------------------------|-----------------|---------------|-----------------------------------------------------------------------------|
| `natls.completion.qualify`                  | `false`, `true` | `false`       | Controls wether variable should always be completed fully qualified         |
| `natls.inlayhints.showAssignmentTargetType` | `false`, `true` | `false`       | Controls wether inlay hints should be shown for target types on assignments |
| `natls.inlayhints.showSkippedParameter`     | `false`, `true` | `true`        | Shows the parameter name of skipped parameter with e.g. `1X`                |
