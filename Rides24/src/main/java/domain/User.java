package domain;

import java.io.Serializable;
import javax.persistence.*;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)  // Estrategia de herencia
@Entity
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String email;  // El correo electrónico será la clave primaria
    public String name;  // Nombre del usuario
    private String password;  // Contraseña del usuario

    public User() {
        super();
    }

    // Constructor con parámetros
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return email.equals(user.email);
    }



    @Override
    public String toString() {
        return email + "; " + name;
    }
}