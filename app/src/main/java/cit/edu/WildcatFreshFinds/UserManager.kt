
package cit.edu.WildcatFreshFinds
object UserManager {
    private var users = ArrayList<User>();

    fun registerUser(fullName : String, email : String, password: String): String{

        if(users.any{it.email == email}){
            return "Email is already taken"
        }

        users.add(User(fullName, email, password))
        return "Registration Successful!"
    }
    fun loginUser(email: String, password: String): Boolean{
        return users.any{it.email == email && it.password == password}
    }

    fun getUsers() {
        println(this.users)
    }
}