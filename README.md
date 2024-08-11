# `AxError`

| <img src="https://github.com/user-attachments/assets/919a96bf-e7c8-4736-9ccb-7b7ce417d253" width="200px"> | <img src="https://github.com/user-attachments/assets/f078968e-e33d-4e00-a317-b6cc1d35cb52" width="200px"> | <img src="https://github.com/user-attachments/assets/1a2fe21a-6d20-4854-9ffc-227a9f03c3f0" width="200px"> | <img src="https://github.com/user-attachments/assets/dcdc059e-5a8b-4d8e-8fb7-0534cf12a35d" width="200px"> |
|:---------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------:|
|                                              **Light Mode**                                               |                                               **Dark Mode**                                               |                                       **Light Mode (changed text)**                                       |                                          **Light Mode (No Log)**                                          |


# 라이브러리 개요

### 설명

- **Exception Handler** 및 **Full Screen 에러 화면**
- 앱 전역에서 `try-catch` **처리되지 않은 (Uncaught) Exception**을 모두 잡아서 `AxErrorActivity`**에러 화면을 자동으로** 띄워주는 기능을 제공한다.
- 이외의 기능적인 오류 상황에서도 개발자 선택에 따라 `AxErrorActivity.Builder()`를 통해 직접 호출하여 띄워줄 수 있다.

### 목적 및 주의 사항

- 해당 라이브러리의 주된 목적은 예기치 못한 에러 상황에서 **앱이 강제 종료되는 것을 방지**하고, 오류 메시지를 안내하는 화면을 제공한다.
- 앱이 강제 종료되었을 때, 안드로이드 시스템에서 발생시키는 **<앱 중지 팝업>** 대신 사용자에게 **보다 친절한 화면을 제공**하며, 에러 화면 내의 **<새로고침>** 버튼을 통해 사용자가 앱으로 다시 돌아올 수 있도록 하여 **앱 이탈률**을 낮추는 것이 주된 목적이다.
- **주의사항** : `AxErrorActivity`는 Full Screen 에러 화면이기 때문에 **사용자에게 중요한 오류임을 꼭 알려야 하는 경우**외에는 해당 에러 화면을 **직접 호출**하여 사용하는 것은 되도록 **권장하지 않는다**.  

### 참고 사항

- 앱 사용 중 Exception이 발생하면 기존의 Process는 소멸되고, 새로운 Process가 생성된다.
- 에러 화면 내의 **<새로고침> 버튼**을 누르면 이전에 보고 있던 화면(Activity)이 **재시작(destroyed and created)** 된다.  
  또한, **액티비티 스택을 활용**하여 재실행함으로써, 화면의 **순서와 Intent 데이터가 모두 유지**된다.
- 에러 화면 내의 Error Log Message 는 Debug 모드에서만 노출된다.
- 에러 화면 **다크 모드 UI 지원**

# 사용 방법

### 1. Exception Handler 등록

- `Application.onCreate()` 함수 내에서 `registerAxExceptionHandler()` 확장 함수를 호출하여, 앱 전역에 에러 핸들러를 등록한다.
- `KakaoSdk.init()` 함수 아래에서 호출해야 한다.
- `AxErrorActivity` 화면 내의 카카오톡 채널에 <문의하기> 버튼을 표시하는 앱의 경우, `kakaoPublicChannelId` 파라미터로 카카오톡 공개 채널 ID를 입력한다.  

#### **[Kotlin Code]**

```kotlin
class KotlinApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_SDK_APP_KEY)
      
        // case 1. AxErrorActivity 화면 내의 카카오톡 채널에 <문의하기> 버튼을 표시하지 않는 앱의 경우
        registerAxExceptionHandler()

        // case 2. AxErrorActivity 화면 내의 카카오톡 채널에 <문의하기> 버튼을 표시하는 경우 (위 KakaoSdk.init() 함수 아래에 호출해야 함!!, otherwise throw exception) 
        registerAxExceptionHandler(
            kakaoPublicChannelId = BuildConfig.KAKAO_CHANNEL_PUBLIC_ID
        )
    }
}
```

#### **[Java Code]**

```java
public class JavaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        KakaoSdk.init(this, BuildConfig.KAKAO_SDK_APP_KEY);

        // case 1. AxErrorActivity 화면 내의 카카오톡 채널에 <문의하기> 버튼을 표시하지 않는 앱의 경우
        registerAxExceptionHandler(this, null);

        // case 2. AxErrorActivity 화면 내의 카카오톡 채널에 <문의하기> 버튼을 표시하는 경우 (KakaoSdk.init() 함수 아래에 호출해야 함!!, otherwise throw exception)
        registerAxExceptionHandler(this, BuildConfig.KAKAO_CHANNEL_PUBLIC_ID);
    }
}
```

### 2. 에러 화면 직접 호출

- `AxErrorActivity.Builder()`를 통해 에러 화면을 직접 호출하여 띄우는 방법

#### **[Kotlin Code]**

```kotlin
try {
  // 네트워크 오류...
} catch (e: IOException) {
    AxErrorActivity.Builder(this)
        .errorTitle(R.string.error_title)
        .errorSubTitle(R.string.error_sub_title)
        .throwable(e)
        .start()
  
    e.printStackTrace()
}


```

#### **[Java Code]**

```java
try {
    // 네트워크 오류...
} catch (IOException e) {
    new AxErrorActivity.Builder(this)
            .errorTitle(R.string.error_title)
            .errorSubTitle(R.string.error_sub_title)
            .throwable(e)
            .start();
    e.printStackTrace();
}
```