AJS.toInit(function() {
    var baseUrl = AJS.$("meta[name='application-base-url']").attr("content");

    function populateForm() {
        AJS.$.ajax({
            url: baseUrl + "/rest/jenkins/1.0/",
            dataType: "json",
            success: function(config) {
                AJS.$("#url").attr("value", config.url);
            }
        });
    }

    function updateConfig() {
        AJS.$.ajax({
            url: baseUrl + "/rest/jenkins/1.0/",
            type: "PUT",
            contentType: "application/json",
            data: '{ "url": "' + AJS.$("#url").attr("value") + '" }',
            processData: false,
            success: function() {
                AJS.messages.success({
                    title: "Success",
                    //FIXME fadeout doesn't work
                    fadeout: true,
                    delay: 2000,
                    duration: 10000,
                    body: "Configuration saved."
                });
            },
            error: function() {
                AJS.messages.error({
                    title: "Error",
                    body: "Unable to save configuration."
                });
            }
        });
    }

    populateForm();

    AJS.$("#admin").submit(function(e) {
        e.preventDefault();
        updateConfig();
    });

    AJS.$(function(){
        AJS.$("#content-2").hide();
        AJS.$("#general-nav-item").click(function(e){
            navigateTo(e.target, "content-1");
        });
        AJS.$("#advanced-nav-item").click(function(e){
            navigateTo(e.target, "content-2");
        });
        function navigateTo(trigger, contentId){
            AJS.$("#main-nav li").removeClass("aui-nav-selected");
            AJS.$(trigger).parent().addClass("aui-nav-selected");
            AJS.$(".nav-content").hide();
            AJS.$("#" + contentId).show();
        }
    })

    AJS.$('#jenkins-integration-administration').parent().addClass('aui-nav-selected');
});