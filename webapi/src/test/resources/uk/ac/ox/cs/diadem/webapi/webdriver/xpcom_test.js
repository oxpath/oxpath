/** Decorate the target function to have special XP priviledges.
 * You need to do this for each individual Javascript function
 */
function UseXPCOM(f) {
    return function() {
        netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
        return f.apply(this, arguments);
    };
}



function saveFile(fileName, val){
   try {
      netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
   } catch (e) {
	console.log("Permission denied");
   }
   
   var file = Components.classes["@mozilla.org/file/local;1"]
             .createInstance(Components.interfaces.nsILocalFile);
   file.initWithPath(fileName);
   console.log("File :" + file);
   if (file.exists() == false) {
     //alert("Creating file... " );
     file.create( Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 420 );
   }
   var outputStream = Components.classes["@mozilla.org/network/file-output-stream;1"]
            .createInstance(Components.interfaces.nsIFileOutputStream);
   outputStream.init( file, 0x04 | 0x08 | 0x20, 420, 0 ); 
   //UTF-8 convert
   var uc = Components.classes["@mozilla.org/intl/scriptableunicodeconverter"]
     .createInstance(Components.interfaces.nsIScriptableUnicodeConverter);
   uc.charset = "UTF-8";
   var data_stream = uc.ConvertFromUnicode(val);
   var result = outputStream.write(data_stream, data_stream.length );
   outputStream.close(); 
}

function tester() {
   console.log("Hallo here I am ");
   var Priviledged = {
   		example: UseXPCOM(function() {
   			console.log("Inside XPCOm");
	   		 saveFile("/home/timfu/test.txt", "Total cool");
   		})
   };
	return   		 saveFile("/home/timfu/test.txt", "Total cool");
}

