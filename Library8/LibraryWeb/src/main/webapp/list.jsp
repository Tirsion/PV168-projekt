<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>

<table border="1">
    <thead>
    <tr>
        <th>id</th>
        <th>title</th>
        <th>autor</th>
        <th>published</th>
        <th>note</th>
    </tr>
    </thead>
    <c:forEach items="${books}" var="book">
        <tr>
            <td><c:out value="${book.id}"/></td>
            <td><c:out value="${book.title}"/></td>
            <td><c:out value="${book.author}"/></td>
            <td><c:out value="${book.published}"/></td>
            <td><c:out value="${book.note}"/></td>
            <td><form method="post" action="${pageContext.request.contextPath}/books/delete?id=${book.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Delete"></form></td>
        </tr>
    </c:forEach>
</table>

<h2>Insert book</h2>
<c:if test="${not empty chyba}">
    <div style="border: solid 1px red; background-color: yellow; padding: 10px">
        <c:out value="${chyba}"/>
    </div>
</c:if>
<form action="${pageContext.request.contextPath}/books/add" method="post">
    <table>
        <tr>
            <th>Book Title:</th>
            <td><input type="text" name="title" value="<c:out value='${param.title}'/>"/></td>
        </tr>
        <tr>
            <th>Author:</th>
            <td><input type="text" name="author" value="<c:out value='${param.author}'/>"/></td>
        </tr>
        <tr>
            <th>Published in:</th>
            <td><input type="text" name="published" value="<c:out value='${param.published}'/>"/></td>
        </tr>
        <tr>
            <th>Note:</th>
            <td><input type="text" name="note" value="<c:out value='${param.note}'/>"/></td>
        </tr>
    </table>
    <input type="Submit" value="Insert" />
</form>

</body>
</html>