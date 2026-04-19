import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
  host: { '[class.is-mobile]': 'isMobile()' },
})
export class App {
  private breakpoints = inject(BreakpointObserver);

  isMobile = toSignal(
    this.breakpoints.observe([Breakpoints.XSmall, Breakpoints.Small]).pipe(
      map((r) => r.matches),
    ),
    { initialValue: false },
  );
}
