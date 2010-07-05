/**
 * This file contains the javascript specific to Tipping Point.  
 */
function showerror(xhr, status, error) {
	// alert('Showing error.');
	var error = jQuery('#errortitle');
	if (error.length > 0) {
		jQuery('#errortitle').html(xhr.statusText);
		jQuery('#errormessage').html('');
		jQuery(xhr.responseXML).find('error').each(function(){
			var message = jQuery(this).find('message').text();
			jQuery('#errormessage').append('<div class="erroritem">' + message + '</div>');
			var trace = jQuery(this).find('trace').text();
			jQuery('#errortrace').append(trace);
		});
		jQuery.blockUI({message: jQuery('#errordialog')});
	}
	else {
		alert('Error returned from call: (' + xhr.status + ') ' + xhr.statusText);
		jQuery.unblockUI();
	}
}