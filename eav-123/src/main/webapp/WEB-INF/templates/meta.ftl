<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-us" lang="en-us">
<head>
    <title>Metadata Editor</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Content-Type" content="text/html"/>
    <style type="text/css" media="screen">
        @import "/eav-123/css/uni-form-generic.css";
        @import "/eav-123/css/uni-form.css";
    </style>
    <script src="/eav-123/js/jquery-1.2.6.min.js" type="text/javascript"></script>
    <script src="/eav-123/js/uni-form.jquery.js" type="text/javascript"></script>
    <script type="text/javascript">
    jQuery(document).ready(function() {
    });
    </script>
</head>

<body>

<center><h1>EAV-123 Metadata</h3></center>

<div class="eav123">
    <div class="eavForm">
        <form method="POST" action="/eav-123/service/meta" class="uniForm">
            <fieldset>
                <div class="ctrlHolder">
                  <label for="metadata">Metadata XML</label>
                  <textarea name="metadata" id="xml" rows="36" cols="120" style="width: 100%; height: auto;">${metadata}</textarea>
                </div>
                <div class="buttonHolder">
                    <button type="submit" class="submitButton">Submit</button>
                </div>
            </fieldset>
        </form>
    </div>
</div>

<center>
<a href="/eav-123/j_spring_security_logout">logout</a>
/
<a href="/eav-123/service/find">find</a>
/
<a href="/eav-123/service/edit">edit</a>
</center>

</body>

</html>