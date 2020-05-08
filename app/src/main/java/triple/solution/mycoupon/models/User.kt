package triple.solution.mycoupon.models

class User(val email: String,
           val name: String,
           val lastName: String,
           var uid: String) {

    constructor() : this("", "", "", ""){

    }
}