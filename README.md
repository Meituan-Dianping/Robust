
# Robust
 
[![Download](https://api.bintray.com/packages/meituan/maven/com.meituan.robust%3Apatch/images/download.svg?version=0.4.99) ](https://bintray.com/meituan/maven/com.meituan.robust%3Apatch/0.4.99/link)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/Meituan-Dianping/Robust/pulls)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://raw.githubusercontent.com/Meituan-Dianping/Robust/master/LICENSE)  

Robust is an Android HotFix solution with high compatibility and high stability. Robust can fix bugs immediately without publishing apk.
 
 [中文说明](README-zh.md)
 
 More help on [Wiki](https://github.com/Meituan-Dianping/Robust/wiki)
 
# Environment

 * Mac Linux and Windows
 * Gradle 2.10+ , include 3.0
 * Java 1.7 +
 
# Usage

1. Add below codes in the module's build.gradle.

	```java
	apply plugin: 'com.android.application'
	//please uncomment fellow line before you build a patch
	//apply plugin: 'auto-patch-plugin'
	apply plugin: 'robust'

	compile 'com.meituan.robust:robust:0.4.99'

2. Add below codes in the outest project's build.gradle file.

	```java
	 buildscript {
	    repositories {
	        jcenter()
	    }
	    dependencies {
	         classpath 'com.meituan.robust:gradle-plugin:0.4.99'
	         classpath 'com.meituan.robust:auto-patch-plugin:0.4.99'
	   }
	}
	```
3. There are some configure items in **app/robust.xml**,such as classes which Robust will insert code,this may diff from projects to projects.Please copy this file to your project.

# Advantages

* Support 2.3 to 10 Android OS
* Perfect compatibility
* Patch takes effect without a reboot
* Support fixing at method level,including static methods
* Support add classes and methods
* Support ProGuard,including inline methods or changing methods' signature

 

When you build APK,you may need to save "mapping.txt" and the files in directory "build/outputs/robust/".

# AutoPatch
 

AutoPatch will generate patch for Robust automatically. You just need to fellow below steps to genrate patches. For more details please visit website http://tech.meituan.com/android_autopatch.html

# Steps

1. Put **'auto-patch-plugin'** just behind **'com.android.application'**，but in the front of others plugins。like this:
	
	```java
	 apply plugin: 'com.android.application'
	 apply plugin: 'auto-patch-plugin'
	```
2. Put **mapping.txt** and **methodsMap.robust** which are generated when you build the apks in diretory **app/robust/**,if not exists ,create it!
3. After modifying the code ,please put annotation `@Modify` on the modified methods or invoke  `RobustModify.modify()` (designed for Lambda Expression )in the modified methods:

	```java
	   @Modify
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	     }
	     //
	     protected void onCreate(Bundle savedInstanceState) {
	        RobustModify.modify()
	        super.onCreate(savedInstanceState);
	     }
	     
	```
	Use annotation `@Add` when you neeed to add methods or classes.
	
	```java
	    //add method
	    @Add
	    public String getString() {
	        return "Robust";
	    }
	    
	    //add class
	    
	    @Add
	    public class NewAddCLass {
	        public static String get() {
	           return "robust";
	         }
	    }
	```
4. After those steps,you need to run the same gradle command as you build the apk,then you will get patches in directory **app/build/outputs/robust/patch.jar**.
5. Generating patches always end like this,which means patches is done
![Success in generating patch](images/patchsuccess_en.png)

# Demo Usage
1. Excute fellow command to build apk：

	```java
	./gradlew clean  assembleRelease --stacktrace --no-daemon
	```
2. After install apk on your phone,you need to save **mapping.txt** and **app/build/outputs/robust/methodsMap.robust**
3. Put mapping.txt and methodsMap.robust which are generated when you build the apks into diretory **app/robust/**,if directory not exists ,create it!
4. After modifying the code ,please put annotation `@Modify` on the modified methods or invoke  `RobustModify.modify()` (designed for Lambda Expression )in the modified methods.
5. Run the same gradle command as you build the apk:

	```java
	./gradlew clean  assembleRelease --stacktrace --no-daemon
	```
6. Generating patches always end like this,which means patches is done
![Success in generating patch](images/patchsuccess_en.png)
7. Copy patch to your phone：

	```java
	adb push ~/Desktop/code/robust/app/build/outputs/robust/patch.jar /sdcard/robust/patch.jar
	```
	patch directory can be configured in ``PatchManipulateImp``.
8. Open app,and click **Patch** button,patch is used.
9. Also you can use our sample patch in **app/robust/sample_patch.jar** ,this dex change text after you click **Jump_second_Activity** Button.
10. In the demo ,we change the text showed on the second activity which is configured in the method ```getTextInfo(String meituan)``` in class ```SecondActivity``` 

# Attentions

1. You should modify inner classes' private constructors to public modifier.
2. AutoPatch cannot handle situations which method returns **this**,you may need to wrap it like belows:

	```java
	method a(){
	  return this;
	}
	```
	changed to 
		
	```java
	method a(){
	  return new B().setThis(this).getThis();
	}
	```
3. Not Support add fields,but you can add classes currently, this feature is under testing.
4. Classes added in patch should  be static nested classes or non-inner classes,and all fields and methods in added class should be public.
5. Support to  fix bugs in constructors currently is under testing.
6. Not support methods which only use fields,without method call or new expression. 
7. Support to resources and so file is under testing.
8. For more help, please visit [Wiki](https://github.com/Meituan-Dianping/Robust/wiki)
## License

    Copyright 2017 Meituan-Dianping

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


