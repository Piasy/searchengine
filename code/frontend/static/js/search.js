url_search = '/api/search';
url_top = '/api/top';
url_related = '/api/related';
num_reco = 5;
num_result = 10;
num_related = 10;
waiting = "#waiting";
type = 0;

function light(text, keywords) {
    keywords = keywords.split(' ');
    for (var i=0; i<keywords.length; i++) {
        var keyword = keywords[i];
        new_keyword = "<span style='color:red'>" + keyword + "</span>";
        old_keyword = new RegExp(keyword, 'g');
        text = text.replace(old_keyword, new_keyword);
        text = text.replace(/　/g, "");
        text = text.replace(/\t/g, "");
        text = text.replace(/\n/g, "");
    }
    return text;
}

function waitingInit(waiting)
{
    var WAITING_WIDTH = 170;
    var WAITING_HEIGHT = 50;
    var text = $("<p></p>", {"style":"text-align:center; font-size:14px; margin-bottom:0px; color:gray;"});
    text.append("Please wait...");
    $(waiting)[0].style.overflow = "visible";
    $(waiting).append(text);
    $(waiting).dialog({modal:true, width:WAITING_WIDTH, height:WAITING_HEIGHT, position:[(document.body.clientWidth-WAITING_WIDTH)/2, (document.body.clientHeight-WAITING_HEIGHT)/2]});
    $(".ui-dialog-titlebar").hide();
    $(waiting).dialog("close");    
}

function waitingShow(waiting)
{
    $(waiting).dialog("open");
}

function waitingHide(waiting)
{
    $(waiting).dialog("close");
}

function refresh(query, page) {
    waitingShow(waiting);
    var query1 = query;
    if (type == 1) query1 = 'type:rrdata ' + query;
    if (type == 2) query1 = 'type:pdf ' + query;
    $.get(url_search, {'query': query1, 'from': page*10-10, 'until': page*10}, function(data){
        var related = data.related;
        if (related != undefined) {
            for (var i=0; i<related.length; i++) {
                $('#related-'+i).html(related[i]);
                $('#related-'+i).show();
            }
            for (var i=related.length; i<num_related; i++) {
                $('#related-'+i).hide();
            }
        }

        var result = data.result;
        if (result != undefined) {
            var flag = true;
            for (var i=0; i<result.length; i++) {
                $('#title-'+i).html(light(result[i].title, query));
                $('#title-'+i)[0].url = result[i].url;
                $('#content-'+i).html(light(result[i].text, query));
                if (result[i].time > 0)
                    $('#time-'+i).html(new Date(result[i].time*1).format('yyyy-mm-dd HH:MM:ss'));
                else
                    $('#time-'+i).html();
                if (result[i].unique == 0) {
                    flag = false;
                    $('#result-'+i).hide();
                } else {
                    $('#result-'+i).show();
                }
                $('#result-'+i)[0].live = 1;
            }
            if (flag)
                $("#result-hide").hide()
            else
                $("#result-hide").show();

            for(var i=result.length; i<num_result; i++) {
                $('#result-'+i).hide();
                $('#result-'+i)[0].live = 0;
            }
        }

        var sp_pic = data.picSpecial;
        if (sp_pic != undefined) {
            if ((sp_pic != "") & (page == 1)) {
                $("#pic-sp-img").attr("src", sp_pic[0].pic);
                $("#pic-sp-text")[0].innerHTML = sp_pic[0].text;
                $("#pic-sp-text")[0].url = sp_pic[0].url;
                $("#pic-sp").show();
            } else
                $("#pic-sp").hide();
        }

        var sp_text = data.textSpecial;
        if (sp_text != undefined) {
            if ((sp_text != "") & (page == 1)) {
                $("#pic-text").show();
                $("#pic-text-title")[0].innerHTML = sp_text[0].title;
                var str = "";
                var content = sp_text[0].content;
                for (var j=0; j<content.length; j++)
                    str += content[j] + '</br>';
                $("#pic-text-content")[0].innerHTML = str;
            } else
                $("#pic-text").hide();
        }
        $('#query')[0].value = query;
        $('#page-now').html(page);
        waitingHide(waiting);
    }, "json");
}


$(document).ready(function(){
    waitingInit(waiting);
    var query = "";
    var page = 1;
    $.get(url_top, {}, function(data){
        var reco = data.result;
        if (reco != undefined) {
            for (var i=0; i<reco.length; i++) {
                $('#reco-'+i).html(reco[i]);
                $('#reco-'+i).show();
            }

            for (var i=reco.length; i<num_reco; i++) {
                $('#reco-'+i).hide();
            }
        }
    }, "json");
    refresh(query, page);
    $("#submit").bind("click", function(){
        query = $("#query")[0].value;
        page = 1;
        refresh(query, page);
    });
    $("#query").keydown(function(e){
        if(e.keyCode==13){
            query = $("#query")[0].value;
            page = 1;
            refresh(query, page);
        }
    });

    $("#page-last").bind("click", function(){
        page -= 1;
        refresh(query, page);
    });
    $("#page-next").bind("click", function(){
        page += 1;
        refresh(query, page);
    });

    for (var i=0; i<num_related; i++) {
        $('#related-'+i).bind('click', function(){
            query = this.innerHTML;
            page = 1;
            refresh(query, page);
        });
    }

    for (var i=0; i<num_reco; i++) {
        $('#reco-'+i).bind('click', function(){
            query = this.innerHTML;
            page = 1;
            refresh(query, page);
        });
    }

    for (var i=0; i<num_result; i++) {
        $('#title-'+i).bind('click', function(){
            window.open(this.url)
        });
    }
    $("#pic-sp-text").bind("click", function(){
        window.open(this.url)
    });
    $("#query").autocomplete({
        serviceUrl:url_related,
        minChars:2,
        delimiter: /(,|;)\s*/,
        maxHeight:400,
        width:300,
        zIndex: 9999,
        deferRequestBy: 0,
        params: { country:'Yes' }, 
    })

    $("#type-input-0").attr('checked', true);
    function choose(x) {
        if (x != 0) $("#type-input-0").attr('checked', false);
        if (x != 1) $("#type-input-1").attr('checked', false);
        if (x != 2) $("#type-input-2").attr('checked', false);
        $("#type-input-"+x).attr('checked', true);
        type = x;
    }

    $("#type-input-0").bind('click', function(){choose(0)});
    $("#type-a-0").bind('click', function(){choose(0)});
    $("#type-input-1").bind('click', function(){choose(1)});
    $("#type-a-1").bind('click', function(){choose(1)});
    $("#type-input-2").bind('click', function(){choose(2)});
    $("#type-a-2").bind('click', function(){choose(2)});
    $("#result-hide").bind("click", function(){
        for (var i=0; i<num_result; i++) {
            if ($('#result-'+i)[0].live == 1) $("#result-"+i).show();
        }
        $("#result-hide").hide();
    })
})
