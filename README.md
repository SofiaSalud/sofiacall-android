# sofiacall-android

To build the example app, you will need to create a `local.properties` file at the root of the project with the following properties:

- `debugGoogleServices` - The Firebase config JSON for a Firebase project to host the realtime database.
- `releaseGoogleServices` - Same as above, but for the release build configuration. You can use the same value if you do not have two separate Firebase projects.
- `debugMixpanelToken` - A Mixpanel API token for logging analytics events.
- `releaseMixpanelToken` - Same as above, but for the release build configuration.

```
debugGoogleServices={"project_info":{"project_number":"123"...
releaseGoogleServices={"project_info":{"project_number":"456"...
debugMixpanelToken=123abc
releaseMixpanelToken=456xyz
```

