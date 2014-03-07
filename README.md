playdocja_issues
================

## What's this

This script gets diffs of documents and register them as git hub issues.


## How to use

Setup the old and new docs to working directory like below.

```
# garbagetown at MBA.local in ~/Desktop [1:19:55]
$ tree
.
|-- 2.1.5
|   `-- manual
|       |-- Highlights.md
|       |-- Home.md
|       |-- Migration.md
|       |-- Modules.md
|       |-- User-Groups-around-the-World.md
|       |-- _Sidebar.md
|       |-- about
|       |   `-- Philosophy.md
(snip)
|-- 2.2.0
|   `-- manual
|       |-- Highlights21.md
|       |-- Highlights22.md
|       |-- Home.md
|       |-- Migration21.md
|       |-- Migration22.md
|       |-- Modules.md
|       |-- _Sidebar.md
|       |-- about
|       |   |-- Philosophy.md
|       |   `-- PlayUserGroups.md
```

Then execute Main.class passing the base dir and versions dirs as program parameters.

The case of above example, program parameters should be specified like below.

- params[0]: /Users/garbagetown/Desktop
- params[1]: 2.1.5
- params[2]: 2.2.0

After getting of diffs has done, the script will ask you the login name, password, repository name, and milestone number.