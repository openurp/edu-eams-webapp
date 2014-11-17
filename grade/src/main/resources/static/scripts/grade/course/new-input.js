/**
 * 倒计时保存成绩的东西，留着万一以后要恢复
 */
var CountDownView = Backbone.View.extend({
	initialize : function() {
		this.maxMinutes = 5;
		this.maxSeconds = this.maxMinutes * 60;

		this.$timePart = this.$el.find(".timePart");
		this.$infoPart = this.$el.find(".infoPart");

		var $infoPart = this.$infoPart;

		this.listenTo(this.options.eventBus, "grade:save-success", _.bind(
				function(message) {
					$infoPart.html(message);
					setTimeout(function() {
						$infoPart.html("").css("background-color", "none");
					}, 2000);
					this.render();
				}, this));

		this.listenTo(this.options.eventBus, "grade:save-failed", _.bind(
				function(message) {
					$infoPart.html(message).css("background-color", "red");
					setTimeout(function() {
						$infoPart.html("").css("background-color", "none");
					}, 2000);
					this.render();
				}, this));

		this.render();
	},
	render : function() {
		var elapsedSeconds = 0;
		var timer = null;
		var maxSeconds = this.maxSeconds;
		var $timePart = this.$timePart;
		var $infoPart = this.$infoPart;
		var eventBus = this.options.eventBus;

		var countDown = function() {
			elapsedSeconds++;
			var restSeconds = maxSeconds - elapsedSeconds;
			var sec = restSeconds % 60;
			var min = Math.floor(restSeconds / 60) % 60;
			var hh = Math.floor(restSeconds / 3600);
			$timePart.html("自动保存倒计时：" + hh + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);

			if (restSeconds <= 0) {
				clearInterval(timer);
				$infoPart.html("暂存成绩中...");
				eventBus.trigger("grade:save");
			}
		};

		timer = setInterval(countDown, 1000);
	}
});

/**
 * 学生成绩的输入区域
 */
var StdGradeView = Backbone.View.extend({
	events : {
		"change [name^='examGrade-'][name$='.examStatus.id']" : "changeExamStatus",
		"change [name^='examGrade-'][name$='.score']" : "changeExamScore",
		"keydown [name^='examGrade-'][name$='.score']" : "focusOnNext"
	},
	initialize : function() {
		this.examStatuses = this.options.examStatuses;
		this.saveAjaxURL = this.options.saveAjaxURL;
		this.eventBus = this.options.eventBus;
		this.lessonId = this.options.lessonId;
		this.scoreFields = this.options.scoreFields;
		this.$resultArea = this.$el.find(".result-area");
		
		this.listenTo(this.eventBus, "grade:tabIndex-order-by-std", _.partial(this.changeTabIndex, "BY_STD"));
		this.listenTo(this.eventBus, "grade:tabIndex-order-by-grade-type", _.partial(this.changeTabIndex, "BY_GRADE_TYPE"));
		this.listenTo(this.eventBus, "grade:before-submit", this.check);
	},
	check : function(result) {
		var $el = this.$el;
		var examStatuses = this.options.examStatuses;
		this.$el.find("[name^='examGrade-'][name$='.score']").each(function(index, ele) {
			var $field = jQuery(ele);
			var examGradeId = $field.attr("name").replace('examGrade-', '').replace('.score', '');
			var $examStatusField = $el.find("[name='examGrade-" + examGradeId + ".examStatus.id']");
			var examStatusId = $examStatusField.val();
			var inputable = examStatuses[examStatusId + ""].inputable;
			if (!$field.is(":hidden") && inputable && $field.val().trim() == "") {
				result.passed = false;
				return false;
			}
		});
	},
	changeTabIndex : function(orderBy) {
		if ("BY_STD" == orderBy) {
			this.$el.find("[name^='examGrade-'][name$='.score']").each(function(index, ele) {
				var $e = jQuery(ele);
				$e.attr("tabindex", $e.attr("tabindex_std")); 
			});
		} else {
			this.$el.find("[name^='examGrade-'][name$='.score']").each(function(index, ele) {
				var $e = jQuery(ele);
				$e.attr("tabindex", $e.attr("tabindex_grade_type")); 
			});
		}
	},
	changeExamStatus : function(event) {
		var $field = jQuery(event.target);
		var examStatusId = $field.val();
		var examGradeId = $field.attr("name").replace('examGrade-', '').replace('.examStatus.id', '');
		var inputable = this.options.examStatuses[examStatusId + ""].inputable;
		
		var $inputField = this.$el.find("[name='examGrade-" + examGradeId + ".score']");
		if (inputable) {
			$inputField.show();	
		} else {
			$inputField.val("");
			$inputField.hide();
			$inputField.change();
		}
	},
	changeExamScore : function(event) {
		// 更新总评
		var $field = jQuery(event.target);
		var examGradeId = $field.attr("name").replace('examGrade-', '').replace('.score', '');
		var $examStatusField = this.$el.find("[name='examGrade-" + examGradeId + ".examStatus.id']");
		var examStatusId = $examStatusField.val();
		var examGradeScore = $field.val();
		
		jQuery.ajax({
			url : this.saveAjaxURL + "?" + new Date().getMilliseconds() + "=1",
			cache : false,
			context : this,
			dataType : "json",
			method : "post",
			data : {
				"lesson.id" : this.lessonId,
				"examGrade.id" : examGradeId,
				"examGrade.examStatus.id" : examStatusId,
				"examGrade.score" : examGradeScore
			},
			beforeSend : function() {
				this.eventBus.trigger("grade:info", "保存成绩...");
			},
			success : function(result) {
				if (result.message) {
					this.eventBus.trigger("grade:error", result.message);
				} else {
					// 根据服务器返回的结果，再次更新考试情况、成绩
					$field.val(result.score);
					$examStatusField.val(result.examStatusId);
					
					// 设置总评成绩
					var refGrade = result.refGrade;
					if (refGrade.passed) {
						this.$resultArea.removeClass("grade-input-unpassed");
						this.$resultArea.addClass("grade-input-passed");
					} else {
						this.$resultArea.removeClass("grade-input-passed");
						this.$resultArea.addClass("grade-input-unpassed");
					}
					this.$resultArea.html(refGrade.scoreText);
					
					this.eventBus.trigger("grade:info", "保存成功");
				}
			},
			error : function(jqXHR, textStatus, errorThrown) {
				// 发生错误，比如超时或者其他原因
				// console.info(jqXHR.responseText);
				this.eventBus.trigger("grade:error", errorThrown);
			}
		});
	},
	focusOnNext : function(event) {
		if (event.which == 13) {
			var $i = jQuery(event.target);
			$i.blur();
			var tabindex = parseInt($i.attr("tabindex"), 10);
			this.scoreFields.each(function(index, e) {
				var $e = jQuery(e);
				if($e.is(":visible") && $e.attr("tabindex") > tabindex) {
					$e.focus();
					$e.select();
					return false;
				}
			});
		}
	}
});

/**
 * 显示消息的区域，比如保存成绩，保存的结果等
 */
var PromptView = Backbone.View.extend({
	initialize : function() {
		this.eventBus = this.options.eventBus;
		this.$text = this.$el.find("span");
		this.listenTo(this.eventBus, "grade:info", this.info);
		this.listenTo(this.eventBus, "grade:error", this.error);
	},
	flash : function(func) {
		if(this.timeoutId) {
			this.$el.hide();
		}
		func();
		this.$el.fadeIn(100);
		var $el = this.$el;
		clearTimeout(this.timeoutId);
		this.timeoutId = setTimeout(function() {
			$el.fadeOut(100);
		}, 3000);
	},
	info : function(message) {
		this.flash(
			_.bind(function() {
				this.$el.removeClass("ui-state-error");
				this.$text.html(message);
			}, this)
		);
	},
	error : function(message) {
		this.flash(
			_.bind(function() {
				this.$el.addClass("ui-state-error");
				this.$text.html(message);
			}, this)
		);
	}
});