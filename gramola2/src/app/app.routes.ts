import { Routes } from '@angular/router';
import { Login } from './login/login';
import { RegisterComponent } from './register/register';
import { PaymentComponent } from './payments/payments';
import { CallbackComponent } from './callback/callback';
import { MusicComponent } from './music/music';
import { ForgotPassword } from './forgot-password/forgot-password'; 
import { ResetPassword } from './reset-password/reset-password';

export const routes: Routes = [
  { path: 'register', component: RegisterComponent },
  { path: 'payment', component: PaymentComponent },
  { path: 'login', component: Login },
  { path: 'callback', component: CallbackComponent },
  { path: 'music', component: MusicComponent },
  { path: '', redirectTo: '/register', pathMatch: 'full' },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'reset-password', component: ResetPassword }
];