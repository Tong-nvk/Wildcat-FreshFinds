
package cit.edu.WildcatFreshFinds
public object UserManager {
    private var users = ArrayList<User>();
    private var signedIn: User? = null;
    fun registerUser(fullName : String, email : String, password: String): String{

        if(users.any{it.email == email}){
            return "Email is already taken"
        }

        users.add(User(fullName, email, password))
        return "Registration Successful!"
    }
    fun loginUser(email: String, password: String): Boolean{
        signedIn = users.find{it.email == email && it.password == password}

        return users.any{it.email == email && it.password == password}
    }

    fun editUser(fullName : String, password : String): Boolean {
        val user = users.find { it.email == getSignedIn()?.email }
        user?.let {
            it.fullName = fullName
            it.password = password
        }
        if(user != null)
        signedIn = user

        return user != null;
    }

    fun signOut() {
        signedIn = null;
    }
    fun getUsers() {
        println(this.users)
    }

    fun getSignedIn(): User? {
        return signedIn;
    }
}