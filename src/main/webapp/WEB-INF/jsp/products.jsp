<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
	
    <title>Product List Page</title>
    <spring:url value="/resources/images" var="images" />

    <style>
    
    <spring:url value="/resources/fonts/glyphicons-halflings-regular.eot"	var="file1" />
    <spring:url value="/resources/fonts/glyphicons-halflings-regular.svg"	var="file2" />
    <spring:url value="/resources/fonts/glyphicons-halflings-regular.ttf"	var="file3" />
    <spring:url value="/resources/fonts/glyphicons-halflings-regular.woff"	var="file4" />
    <spring:url value="/resources/fonts/glyphicons-halflings-regular.woff2"	var="file5" />
    
	@font-face {
    font-family: 'Glyphicons Halflings';

    src: url('${file1}');
    src: url('${file1}?#iefix') format('embedded-opentype'), url('${file4}') format('woff'), url('${file3}') format('truetype'), url('${file2}#glyphicons_halflingsregular') format('svg');
	}
    </style>
    
	<spring:url value="/resources/js/jquery-3.1.0.min.js" var="resourceJquery" />
	<script src="${resourceJquery}"></script>
	<spring:url value="/resources/js/bootstrap.min.js" var="resourceBootstrapJs" />
	<script src="${resourceBootstrapJs}"></script>
	
</head>

<body>
	<!-- Nav-bar -->
	<jsp:include page="navbar.jsp"/>
	<!-- /.Nav-bar -->

    <!-- Page Content -->
    <div class="container">

        <!-- Page Heading -->
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Shoes
                    <small>Select categories or search using search box</small>
                </h1>
            </div>
        </div>
        <!-- /.row -->
		
        <div class="row">
			<div class="row">
				<!-- left category  -->
	            <div class="col-md-3">
	                <p class="lead">Top Categories</p>
	                <div id="category-options" class="list-group">
						<c:if test="${not empty categoryList}">
							<c:forEach var="listValue" items="${categoryList}">
								<a href="<c:url value='products-by-category-${listValue}' />" class="list-group-item category-select-option" data-category-name="${listValue}">${listValue}</a>
							</c:forEach>
						</c:if>
	                </div>
	            </div>
				
				<div class="col-md-9">
					<c:if test="${not empty productList}">
						<c:set var="divCount" scope="page" value="0"/>
						<div class="row">
						<c:forEach var="listValue" items="${productList}">
				            <div class="col-md-3 col-sm-12 col-xs-12 portfolio-item">
				                <a href="<c:url value='product-details-${listValue.getProductCode()}' />">
				                    <img class="img-responsive" src="${images}/${listValue.getImageUrl()}" alt="">
				                </a>
    							<p >${listValue.getProductName()}</p>
				            </div>
				            <c:set var="divCount" value="${divCount + 1}" />
				            <c:if test="${divCount % 3 == 0}">
		            	</div>
		            	<div class="row">
				            </c:if>
						</c:forEach>
						</div>
					</c:if>
				</div>
				
			</div>
		</div>
			
        <hr>

        <!-- Footer -->
        <footer>
            <div class="row">
                <div class="col-lg-12">
                    <p>Copyright &copy; Divilioglu LTD. 2016</p>
                </div>
            </div>
            <!-- /.row -->
        </footer>

    </div>
    <!-- /.container -->

</body>

</html>