<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="com.tippingpoint.database.Schema"%>
<%@page import="com.tippingpoint.sql.ConnectionManager"%>
<%@page import="com.tippingpoint.sql.ConnectionManagerFactory"%>
<%@page import="com.tippingpoint.database.Table"%>
<%@page import="com.tippingpoint.database.ColumnDefinition"%>
<%@page import="com.tippingpoint.database.Column"%>
<%@page import="com.tippingpoint.database.Constraint"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>ConScan Administration</title>
		<link type="text/css" rel="stylesheet" href="conscan.css">
		<script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
	</head>
	<body>
		<h1>ConScan Administration.</h1>
		<hr>
		<h2>Database</h2>
		<%
		ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
		Schema schema = manager.getSchema(manager.getConnectionSource().getSchema());
		if (schema != null) {
			%>
			<h3><%=schema.getName()%></h3>
			<%
			Iterator<Table> iterTables = schema.getTables();
			if (iterTables != null && iterTables.hasNext()) {
				while (iterTables.hasNext()) {
					Table table = iterTables.next();
					%>
					<table>
						<thead>
							<tr>
								<td colspan="3"><%=table.getName()%></td>
								<td>
									<a class="dro" href="javascript: drop('<%=table.getName()%>');">drop</a>
								</td>
							</tr>
						</thead>
						<tbody>
							<%
							Iterator<ColumnDefinition> iterColumns = table.getColumns();
							if (iterColumns != null && iterColumns.hasNext()) {
								while (iterColumns.hasNext()) {
									ColumnDefinition column = iterColumns.next();
									%>
									<tr>
										<td><%=column.getName()%></td>
										<td><%=column.getType()%></td>
										<td><%=(column.isRequired() ? "NOT " : "") + "NULL"%></td>
									</tr>
									<%
								}
							}
							List<Constraint> listConstraints = table.getConstraintList();
							if (listConstraints != null && !listConstraints.isEmpty()) {
								for (Constraint constraint : listConstraints) {
									%>
									<tr>
										<td><%=constraint.getName()%></td>
										<td><%=constraint.getType()%></td>
										<td>
											<%
											Iterator<Column> iterConstraintColumns = constraint.getColumns();
											if (iterConstraintColumns != null && iterConstraintColumns.hasNext()) {
												while (iterConstraintColumns.hasNext()) {
													%>
													iterConstraintColumns.next();<%=iterConstraintColumns.hasNext() ? "<br/>" : ""%>
													<%
												}
											}
											%>
										</td>
									</tr>
									<%
								}
							}
							%>
						</tbody>
					</table>
					<br/>
					<%
				}
			}
		}
		else {
			%>
			No schema available.
			<%
		}
		%>
	<script type="text/javascript">
		function drop(object) {
			jQuery.ajax({
				type: "DELETE",
				url: 'database/' + object,
				success: function(data) {
					alert('Drop of ' + object + ' was successful.');
					window.location.href = "admin.jsp";
				},
				error: function(xhr, status, error) {
					alert('Uh oh, drop of ' + object + ' failed: ' + xhr.statusText);
				} 
			});
		}
	</script>
	</body>
</html>