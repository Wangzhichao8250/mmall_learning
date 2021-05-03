<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>
<h2>Hello World!</h2>

springMvc上传文件
    <form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
        <input type="file" name="upload_File"/>
        <input type="submit" name="上传文件" value="upload"/>
    </form>

<%--富文本图片上传文件--%>
    <%--<form name="form1" action="/manage/product/richText_img_upload.do" method="post" enctype="multipart/form-data">--%>
        <%--<input type="file" name="upload_File"/>--%>
        <%--<button type="submit" value="富文本图片上传文件"/>--%>
    <%--</form>--%>
</body>
</html>
