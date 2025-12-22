import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // Importa CommonModule
import { FormsModule } from '@angular/forms'; 
import { UserService } from '../user';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  bar?: string;
  email?: string;
  pwd1?: string;
  pwd2?: string;
  clientId?: string;
  clientSecret?: string;
  registroOK : boolean = false;
  registroKO : boolean = false;
 

  constructor(private service : UserService) { }

  registrar() {
    this.registroOK = false;
    this.registroKO = false;
    
    if (this.pwd1 !== this.pwd2) {
      console.error('Las contraseñas no coinciden');
      this.registroKO = true;
      return;
    }
    const body = {
      bar: this.bar!,
      email: this.email!,
      pwd1: this.pwd1!,
      pwd2: this.pwd2!,
      clientId: this.clientId!,
      clientSecret: this.clientSecret!
    };
    console.log('Enviando al backend:', body);

    this.service.register(this.bar!, this.email!, this.pwd1!, this.pwd2!, this.clientId!, this.clientSecret!).subscribe({
      next: () => {
        this.registroOK = true;
      },
      error: (err) => {
        console.error('Error recibido del backend:', err); // Esto también puede ayudar
        console.error(err.error?.message || 'Error en el registro');
        this.registroKO = true;
      }
    });
  }
}

