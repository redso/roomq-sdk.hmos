# NoQ SDK for HarmonyOS

## Introduction

This project contains a demo project and a roomq module which is used to export har file. The demo project demonstrates how to integrate with the the exported har file to perform RoomQ services. 


## Prerequisite

Java 8 (HarmonyOS currently supports Java 8 and does not support Kotlin yet)


## Demo Project

### How to start the demo project

1. Sync Gradle
2. Open Tools > Device Manager
3. Login in harmony developer account
4. Select your virtual devices (Phone or Tablet)
5. On the IDE panel, select entry as Run/Debug Configuration
6. Make sure you select the virtual devices and click Run button

### Configuration

As the project need to open a webview, we have to add **ohos.permission.INTERNET** permission to **config.json**. We also have to register hk.noq.roomq.WaitingRoomAbility from the har file in order for the bundle manager to fetch it.

```jsx
"module": {
		...
		"reqPermissions": [
	      {
	        "name": "ohos.permission.INTERNET"
	      }
    ],
		"abilities": [
				{
	        "orientation": "unspecified",
	        "name": "hk.noq.roomq.WaitingRoomAbility",
	        "description": "$string:mainability_description",
	        "label": "$string:mainability_description",
	        "type": "page",
	        "launchType": "standard",
	        "visible": true,
	        "mergeRule": {
	          "replace":  ["description", "label"] // To overrdie the default description and label
	        }
	      }
		],
		...
}
```

Be careful, the added ability can be found by the bundle name of the demo project, instead of "hk.noq.roomq", and the ability name "hk.noq.roomq.WaitingRoomAbility".

```jsx
Intent waitingRoomIntent = new Intent();
                    Operation operation = new Intent.OperationBuilder()
                            .withDeviceId("")
                            .withBundleName(context.getBundleName())
                            .withAbilityName("hk.noq.roomq.WaitingRoomAbility")
                            .build();
                    waitingRoomIntent.setOperation(operation);
                    String waitingRoomUrl = enqueueResult.waitingRoomURL + "?noq_t=" + enqueueResult.id + "&c=" + clientID + "&noq_r=https://app.noq.com.hk?p=hmos";
                    waitingRoomIntent.setParam("URL",waitingRoomUrl);
```

In the build.gradle under the demo project directory, we add google gson library as a dependence and libraries from entry > libs

```jsx
dependencies {
		implementation fileTree(dir: 'libs', include: ['*.jar', '*.har']) //implement all har or js from libs
		implementation 'com.google.code.gson:gson:2.8.8'
}
```


## roomq module

### How to build har from the module

1. Expand Gradle from right panel.
2. Under roomq > Tasks, expand ohos:debug or ohos:release
3. Click on packageDebugHar or packageReleaseHar to build a har file.
4. Copy har file from roomq > build > outputs > debug/release directory to entry > libs
5. Sync Project