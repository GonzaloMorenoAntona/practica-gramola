import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { RegisterComponent } from './register/register';
import { Login } from "./login/login";
import { PaymentComponent } from "./payments/payments";
import { UserService } from './user'; 
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RegisterComponent, Login, PaymentComponent, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  constructor(public userService: UserService) {}
  protected readonly title = signal('gramola2');
}
