import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Geolocalizacion } from './geolocalizacion';

describe('Geolocalizacion', () => {
  let component: Geolocalizacion;
  let fixture: ComponentFixture<Geolocalizacion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Geolocalizacion]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Geolocalizacion);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
