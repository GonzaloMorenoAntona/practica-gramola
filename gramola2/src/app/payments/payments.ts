import { Component, OnInit } from '@angular/core';
import { PaymentService } from '../payment-service';
import { Router } from '@angular/router';

declare let Stripe: any

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [],
  templateUrl: './payments.html',
  styleUrl: './payments.css'
})
export class PaymentComponent implements OnInit {

  stripe = new Stripe("pk_test_51SIV0yRm0ClsCnoVWXB3iOiEfdtda0z61OvJDYLWIIAq5FQZuIdFOAb4sEwtk8w2eEooAbJXOSKxsuGw3j56g5G900aYokx6Qx")
  transactionDetails: any;
  token? : string
  constructor(private PaymentService: PaymentService, private router : Router) { }

  ngOnInit(): void {
    const params = this.router.parseUrl(this.router.url).queryParams;
    this.token = params['token'];
  }

  prepay() {
    this.PaymentService.prepay().subscribe({
      next: (response: any) => {
        this.transactionDetails = JSON.parse(response.body)
        this.showForm()
      },
      error: (response: any) => {
        alert(response)
      },
    })
  }

  showForm() {
    let elements = this.stripe.elements()
    let style = {
      base: {
        color: "#32325d", fontFamily: 'Arial, sans-serif',
        fontSmoothing: "antialiased", fontSize: "16px",
        "::placeholder": {
          color: "#32325d"
        }
      },
      invalid: {
        fontFamily: 'Arial, sans-serif', color: "#fa755a",
        iconColor: "#fa755a"
      }
    }
    let card = elements.create("card", { style: style })
    card.mount("#card-element")
    card.on("change", function (event: any) {
      document.querySelector("button")!.disabled = event.empty;
      document.querySelector("#card-error")!.textContent =
        event.error ? event.error.message : "";
    });
    let self = this
    let form = document.getElementById("payment-form");
    form!.addEventListener("submit", function (event) {
      event.preventDefault();
      self.payWithCard(card);
    });
    form!.style.display = "block"
  }

  payWithCard(card: any) {
    let self = this
    this.stripe.confirmCardPayment(this.transactionDetails.data.client_secret, {
      payment_method: {
        card: card
      }
    }).then(function (response: any) {
      if (response.error) {
        alert(response.error.message);
      } else {
        if (response.paymentIntent.status === 'succeeded') {
          self.PaymentService.confirm(self.transactionDetails.id, self.token!).subscribe({
            next: () => {
              self.router.navigate(["/login"]);
           },
            error: (err) => {
              alert("Error al confirmar el pago: " + err.message);
            }
          });
        }
      }
    });
  }
  

}

