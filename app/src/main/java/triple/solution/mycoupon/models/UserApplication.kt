package triple.solution.mycoupon.models

class UserApplication(val email: String, val userType: Int) {
    constructor() : this("", 0){
    }
}