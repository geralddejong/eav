<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-us" lang="en-us">
<head>
    <title>Entity Editor</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Content-Type" content="text/html"/>

    <style type="text/css" media="screen">
        @import "/eav-123/css/uni-form-generic.css";
        @import "/eav-123/css/uni-form.css";
        @import "/eav-123/css/datePicker.css";
    </style>
    <script src="/eav-123/js/jquery-1.2.6.min.js" type="text/javascript"></script>
    <script src="/eav-123/js/uni-form.jquery.js" type="text/javascript"></script>
    <script src="/eav-123/js/jquery.datePicker.js" type="text/javascript"></script>
    <script src="/eav-123/js/date.js" type="text/javascript"></script>

    <script type="text/javascript">
    jQuery(document).ready(function() {
        jQuery(function() {
            jQuery('.eavTimestampField').datePicker({startDate:'01/01/1996'});
        });
        jQuery('.eavTimestampField').css('width','25%');
        jQuery("fieldset").each(
           function(e) {
               $(this).attr('class', 'inlineLabels');
           }
        );
        /*
        jQuery("#rating a").click(
            function(e){
             e.preventDefault();
             $.post("rate.php", {rating: $(this).html()}, function(xml) {
               // format and output result
               $("#rating").html(
                 "Thanks for rating, current average: " +
                 $("average", xml).text() +
                 ", number of votes: " +
                 $("count", xml).text()
               );
             });
           }
        );
        */
    });
    </script>
</head>

<body>

<center><h1>EAV-123 Entity Editor</h3></center>

<#assign baseUrl = baseUrl>
<#assign path = path>

${entity.toHtml(baseUrl, path)}

<#if error?? >
<div id="errorMsg">
    <center><h3>${error}</h3></center>
</div>
</#if>

<center>
<a href="/eav-123/j_spring_security_logout">logout</a>
/
<a href="/eav-123/service/find">find</a>
/
<a href="/eav-123/service/meta">meta</a>
</center>

</body>

</html>