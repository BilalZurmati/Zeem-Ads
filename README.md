# Zeem-Ads

**Module Integration:** <br/>
Here is how we can integrate this ads module into our app<br/>
Project has different branches make sure to download the code from required branch.<br/> <br/> 
Download **zeem** module from the project and paste it in your project's root folder.<br/><br/>
In **Settings.Gradle** add this<br/><br/>
Kotlin DSL:<br/>
```
include(":zeem")
```

Grovy:<br/>
```
include(':zeem')
```
<br/>

in **Project level** gradle add this<br/>

For Grovy:<br/>
```
maven { url "https://jitpack.io" }
```
For Kotlin DSL:<br/>
```
maven { url = uri("https://www.jitpack.io" ) }
```
<br/>


in **App level** gradle<br/>
```
implementation(project(":zeem"))
``` 

<br/>Add this to **Manifest**<br/>

```<meta-data
      android:name="com.google.android.gms.ads.APPLICATION_ID"
      android:value="YOUR_ADMOB_APP_ID" />
```

### AdsManager
This class is available in ads package, this class works as the manager which manages ads request, cahce ads, show ads. It works as a stand alone manager to handle the ad related queries, we will simply call this class methods to show and load ads whenever we need it.


#### Initialize module

Before initialzing this module first we will provide the required data
```
AdsManager.adData.bannerId = "admob_banner_ad_id_here"
AdsManager.adData.interstitialId = "admob_interstitial_ad_id_here"
AdsManager.adData.nativeId = "admob_native_ad_id_here"
AdsManager.adData.clickCapping = 3  // you can change this value as per your strategy
AdsManager.adData.BannerRequests = 2  //number of banner ads in each session
AdsManager.adData.interstitialRequests = 2  //number of interstitial ads in each session
AdsManager.adData.nativeRequest = 2   //number of native ads in each session

```
Call below method on splash to make this module start requesting ads for you
 ```
initAdManager(this, this)
```


#### Native Ad
Below method will inflate native ad
 ```
 showNativeAd(context: Context,container: FrameLayout,layout: Layout) {inflated->
                  returns true if ad is inflated or else it returns false
    }
```

just simply pass context, framelayout container where you want to inflate native Ad and Layout Type.

Here are Layout Types

Layout.FULL<br/>
Layout.SIDE_MEDIA<br/>
Layout.NO_MEDIA<br/>
Layout.SIDE_ICON<br/>

#### Banner Ad
 ```
 loadBannerAd(context: Context, container: FrameLayout) {inflated->
                  returns true if ad is inflated or else it returns false
    }
```

#### Interstitial Ad
 ```
 showInterstitialAd(activity: Activity,dismiss: InterstitialDismiss)  {dismissEvent->
                Do your next event here.
    }
```

For InterstitialDismiss use one of this

 InterstitialDismiss.ON_CLOSE<br/> InterstitialDismiss.ON_IMPRESSION<br/>


