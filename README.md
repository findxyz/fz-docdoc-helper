
## ☆☆☆在idea中打包该swing项目的步骤说明☆☆☆

### 1.关闭idea中所有打开的标签页

### 2.打开idea form相关的java和form文件，显示form页（若还不行换成java页，有时可能需要多试几次。。。）

### 3.使用idea的rebuild重新编译项目

### 4.使用mvn的package进行打包

#### 之所以要按照上述步骤进行打包是因为，idea的swing项目中有使用到idea自身的一些classes，通过mvn直接打包无法包含这些需要的classes，故在启动时会报Form的空指针异常
