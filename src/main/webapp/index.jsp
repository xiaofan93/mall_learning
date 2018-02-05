<%@ page language="java"  contentType="text/html;  charset=utf-8" %>

<html>
<body>
<h2>Hello World!</h2>

springmvc文件上传

<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="springmvc文件上传" />
</form>

<form name="form1" action="/manage/product/rich_text.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="富文本文件上传" />
</form>


</body>
</html>
