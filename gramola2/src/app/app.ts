import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { RegisterComponent } from './register/register';
import { Login } from "./login/login";
import { PaymentComponent } from "./payments/payments";


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RegisterComponent, Login, PaymentComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('gramola2');
}
