[#ftl]
{
	"examStatusId" : "${(examGrade.examStatus.id)!}",
	"examStatusName" : "${(examGrade.examStatus.name)!}",
   	"score" : "${(examGrade.score)!}",
   	"passed" : ${examGrade.passed?string("true", "false")},
   	[#-- 相关的在界面上要更新的成绩 --]
	"refGrade" : {
	   	"scoreText" : "${(refGrade.scoreText)!}",
	   	"passed" : ${(refGrade.passed?string("true", "false"))!("false")}
	}
}