<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="com.tippingpoint.database.ColumnDefinition"%>
<%@page import="com.tippingpoint.database.Column"%>
<%@page import="com.tippingpoint.database.Constraint"%>
<%@page import="com.tippingpoint.database.ForeignKeyConstraint"%>
<%@page import="com.tippingpoint.database.Schema"%>
<%@page import="com.tippingpoint.database.Table"%>
<%@page import="com.tippingpoint.sql.ConnectionManager"%>
<%@page import="com.tippingpoint.sql.ConnectionManagerFactory"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>ConScan Administration</title>
		<link type="text/css" rel="stylesheet" href="css/conscan.css">
		<script type="text/javascript" src="js/jquery-1.7.min.js"></script>
		<script type="text/javascript" src="js/jquery.blockUI.2.39.js"></script>
		<script type="text/javascript" src="js/ajaxupload.js"></script>
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
			<div>
				<%
				Iterator<Table> iterTables = schema.getTables();
				if (iterTables != null && iterTables.hasNext()) {
					while (iterTables.hasNext()) {
						Table table = iterTables.next();
						%>
						<table>
							<thead>
								<tr>
									<td>
										<a href="javascript: drop('<%=table.getName()%>');">
											<img src="images/delete-icon.png"/>
										</a>
									</td>
									<td colspan="3"><%=table.getName()%></td>
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
											<td>&nbsp;</td>
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
											<td>
												<%
												if (ForeignKeyConstraint.TYPE.equals(constraint.getType())) {
													%>
													<a href="javascript: drop('<%=table.getName()%>', '<%=constraint.getName()%>');">
														<img src="images/delete-icon.png"/>
													</a>
													<%
												}
												else {
													%>
													&nbsp;
													<%
												}
												%>
											</td>
											<td><%=constraint.getName()%></td>
											<td><%=constraint.getType()%></td>
											<td>
												<%
												Iterator<Column> iterConstraintColumns = constraint.getColumns();
												if (iterConstraintColumns != null && iterConstraintColumns.hasNext()) {
													while (iterConstraintColumns.hasNext()) {
														%>
														<%=iterConstraintColumns.next()%><%=iterConstraintColumns.hasNext() ? "<br/>" : ""%>
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
				%>
			</div>
			<%
		}
		else {
			%>
			No schema available.
			<%
		}
		%>
		<div id="importupload" class="button">Import</div>
		<div id="errordialog" class="hidden">
			<div id="errortitle" class="erroritem"></div>
			<div id="errormessage" class="erroritem"></div>
			<div id="errortrace" class="hidden erroritem"></div>
			<br/>
			<div class="erroritem">
				<input type="button" id="errorcontinue" value="Continue"/>
			</div>
		</div> 	
		<script type="text/javascript">
			function drop(table, object) {
				var url = 'database/' + table;
				if (typeof object != 'undefined') {
					url += '/' + object;
				}

				jQuery.blockUI();
				jQuery.ajax({
					type: "DELETE",
					url: url,
					success: function() {
						window.location.href = "admin.jsp";
					},
					error: function(xhr, status, error) {
						jQuery('#errortitle').html(xhr.statusText);
						jQuery('#errormessage').html('');
						jQuery(xhr.responseXML).find('error').each(function(){
							var message = jQuery(this).find('message').text();
							jQuery('#errormessage').append('<div class="erroritem">' + message + '</div>');
							var trace = jQuery(this).find('trace').text();
							jQuery('#errortrace').append(trace);
						});
			            jQuery.blockUI({ message: jQuery('#errordialog')}); 
					} 
				});
			}

			jQuery(document).ready(function() {
				jQuery('#errorcontinue').click(function() { 
		            jQuery.unblockUI();
		        });

				new AjaxUpload('importupload', {
					action: 'database',
					// Fired before the file is uploaded 
					// You can return false to cancel upload
					// @param file basename of uploaded file
					// @param extension of that file
					onSubmit: function(file, extension) {
			            jQuery.blockUI({ message: 'Uploading ' + file}); 
					},
					// Fired when file upload is completed
					// WARNING! DO NOT USE "FALSE" STRING AS A RESPONSE!
					// @param file basename of uploaded file
					// @param response server response
					onComplete: function(file, response) {
						//alert('complete');
			            jQuery.unblockUI();
					}
				});
		    }); 	        
		</script>
	</body>
</html>