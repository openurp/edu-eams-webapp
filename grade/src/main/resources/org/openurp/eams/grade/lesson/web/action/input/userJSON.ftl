[#ftl]
{
users : [[#list users! as user]{id : '${user.id}', name : '${user.name?js_string}', fullname : '${user.fullname?js_string}'}[#if user_has_next],[/#if][/#list]]
}