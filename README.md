
# Robust
 

Robust is an Android HotFix solution with high compatibility and high stability.Robust can fix bugs immediately without publishing apk.
 
 [中文说明](README-zh.md)
 
# Environment

 * Mac or Linux 
 * Gradle 2.10+  
 * Java 1.7 +
 
# Usage

1. Add below codes in the module's build.gradle.

	```java
	apply plugin: 'com.android.application'
	//please uncomment fellow line before you build a patch
	//apply plugin: 'auto-patch-plugin'
	apply plugin: 'robust'
	compile 'com.meituan.robust:robust:0.3.0'
	```
2. Add below codes in the outest project's build.gradle file.

	```java
	 buildscript {
	    repositories {
	        jcenter()
	    }
	    dependencies {
	         classpath 'com.meituan.robust:gradle-plugin:0.3.0'
	         classpath 'com.meituan.robust:auto-patch-plugin:0.3.0'
	   }
	}
	```
3. There are some configure items in **app/robust.xml**,such as classes which Robust will insert code,this may diff from projects to projects.Please copy this file to your project.

# Advantages

* Support 2.3 to 7.X Android OS
* Perfect compatibility
* Patch takes effect without reboot
* Support fixing at method level,including static methods
* Support add classes and methods
* Suport ProGuard,including inline methods or changing methods' signure

 

When you build APK,you may need to save mapping.txt and files in build/outputs/robust/methodsMap.robust.

# AutoPatch
 

AutoPatch will generate patch for Robust automatically.You just need to fellow below steps to genrate patches.

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


# Demo Usage：
1. Excute fellow command to build apk：

	```java
	./gradlew clean  assembleRelease --stacktrace --no-daemon
	```
2. After install apk on your phone,you need to save **mapping.txt** and **app/build/outputs/robust/methodsMap.robust**
3. Put mapping.txt and methodsMap.robust which are generated when you build the apks in diretory **app/robust/**,if not exists ,create it!
4. After modifying the code ,please put annotation `@Modify` on the modified methods or invoke  `RobustModify.modify()` (designed for Lambda Expression )in the modified methods.
5. Run the same gradle command as you build the apk:

	```java
	./gradlew clean  assembleRelease --stacktrace --no-daemon
	```
6. Copy patch to your phone：

	```java
	adb push ~/Desktop/code/robust/app/build/outputs/robust/patch.jar /sdcard/robust/patch_temp.jar
	```
	patch directory can be configured in ``PatchManipulateImp``.
7. Open app,and click patch button,patch is used.
 
8. Also you can use our sample dex in **app/robust/sample_patch.dex** ,this dex change text after you click **Jump_second_Activity** Button.

9. Demo delete patch after used.You should copy patch everytimes.

# Attentions

1. You should modify inner classes' priavte constructors to public modifier.
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
3. Not Support add fields,but you can add classes.
4. Classes added in patch should  be static nested classes or non-inner classes,and all fields and methods in added class should be public.
5. Not suport fix bugs in constructors.
6. Not support methods which only use fields,without method call or new expression. 

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


