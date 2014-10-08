<!-- translated -->
<!-- # Play 2.1 migration guide -->
# Play 2.1 移行ガイド

<!--
To migrate a **Play 2.0.x** application to **Play 2.1.0** first update Play's `sbt-plugin` in the `project/plugins.sbt` file:
-->
**Play 2.0.x** アプリケーションを **Play 2.1.0** に移行するためには、まず `project/plugins.sbt` 内でPlayの `sbt-plugin` のバージョンを上げましょう。

```
addSbtPlugin("play" % "sbt-plugin" % "2.1.0")
```

<!--
Now update the `project/Build.scala` file to use the new `play.Project` class instead of the `PlayProject` class:
-->
次に `project/Build.scala` 内で `PlayProject` の代わりに新しい `play.Project` を使うように修正しましょう。

<!--
First the import:
-->
import を記述します。

```
import play.Project._
```

<!--
Then the `main` project creation:
-->
`main` プロジェクトの生成部分を次のように変更します。

```
val main = play.Project(appName, appVersion, appDependencies).settings(
```

<!--
Lastly, update your `project/build.properties` file:
-->
`project/build.properties` ファイルを次のようにアップデートします。

```
sbt.version=0.12.2
```

<!--
Then clean and re-compile your project using the `play` command in the **Play 2.1.0** distribution:
-->
最後に **Play 2.1.0** のディストリビューションに含まれる `play` コマンドを起動して、プロジェクトを一旦 clean して、そして再コンパイルしましょう。

```
play clean
play ~run
```

<!--
If any compilation errors cropped up, this document will help you figure out what deprecations or incompatible changes may have caused the errors.
-->
もし何らかのコンパイルエラーが発生したら、このドキュメントを参考にして、エラーの原因となった非推奨機能、互換性のない変更などを発見することができます。
