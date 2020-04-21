package triple.solution.mycoupon.models

class User(val email: String,
           val name: String,
           val lastName: String,
           val cellPhone: String,
           var uid: String,
           var typeClient: Int? = 0) {

    constructor() : this("", "", "", "", ""){

    }
}