<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Get HTTP</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/app/css/styles.css" />
    </head>
    <body>
        <div class="container">
            <div id="wrapper">
                <h1 id="title">GET HTTP Request Information</h1>
                <table>
                    <tr>
                        <th>Endpoint</th>
                        <th>HTTP Method</th>
                        <th>Received At</th>
                    </tr>
                    <tr>
                        <td>${requestInfo.endpoint}</td>
                        <td>${requestInfo.httpMethod}</td>
                        <td>${requestInfo.receivedAt}</td>
                    </tr>
                </table>
            </div>
            <jsp:include page="../layout/footer.jsp" />
        </div>
    </body>
</html>
