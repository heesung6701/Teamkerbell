package org.teamfairy.sopt.teamkerbell.model.assist

import org.teamfairy.sopt.teamkerbell.model.data.RoleFeedback
import org.teamfairy.sopt.teamkerbell.model.data.TaskResponse

class TaskResponseWithFeedback {
    var taskResponse: TaskResponse? = null
    var feedbacks: ArrayList<RoleFeedback>? = null
}