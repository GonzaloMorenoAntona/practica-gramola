import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet, Router } from '@angular/router';
import { UserService } from './user'; 
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  constructor(public userService: UserService, private router: Router) {}
  protected readonly title = signal('gramola2');
  logout() {
    this.userService.logout(); // Borra sesión
    this.router.navigate(['/login']); // Te manda fuera
  }
}

