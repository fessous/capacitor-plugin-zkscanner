# capacitor-plugin-zkscanner

This plugin allows for easy cordova integration with the zkteco ZK9500 fingerprint reader

## Install

```bash
npm install capacitor-plugin-zkscanner
npx cap sync
```

## API

<docgen-index>

* [`openDevice()`](#opendevice)
* [`closeDevice()`](#closedevice)
* [`addListener('CaptureEvent', ...)`](#addlistenercaptureevent)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### openDevice()

```typescript
openDevice() => Promise<OpenDeviceResponse>
```

**Returns:** <code>Promise&lt;<a href="#opendeviceresponse">OpenDeviceResponse</a>&gt;</code>

--------------------


### closeDevice()

```typescript
closeDevice() => Promise<CloseDeviceResponse>
```

**Returns:** <code>Promise&lt;<a href="#closedeviceresponse">CloseDeviceResponse</a>&gt;</code>

--------------------


### addListener('CaptureEvent', ...)

```typescript
addListener(eventName: 'CaptureEvent', listenerFunc: (data: { data: any; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                           |
| ------------------ | ---------------------------------------------- |
| **`eventName`**    | <code>'CaptureEvent'</code>                    |
| **`listenerFunc`** | <code>(data: { data: any; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### Interfaces


#### OpenDeviceResponse

| Prop          | Type                 |
| ------------- | -------------------- |
| **`success`** | <code>boolean</code> |
| **`error`**   | <code>string</code>  |


#### CloseDeviceResponse

| Prop          | Type                 |
| ------------- | -------------------- |
| **`success`** | <code>boolean</code> |
| **`error`**   | <code>string</code>  |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
