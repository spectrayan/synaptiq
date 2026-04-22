import {
  Component,
  input,
  effect,
  ElementRef,
  viewChild,
  afterNextRender,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartSpec } from '@synaptiq/constants';
import * as echarts from 'echarts';

@Component({
  selector: 'syn-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="chart-container">
      @if (spec().title) {
        <h3 class="chart-title">{{ spec().title }}</h3>
      }
      <div
        #chartEl
        class="chart-canvas"
        [style.height.px]="spec().height || 320"
      ></div>
    </div>
  `,
  styleUrl: './chart.component.scss',
})
export class ChartComponent implements OnDestroy {
  readonly spec = input.required<ChartSpec>();
  private readonly chartEl = viewChild<ElementRef<HTMLDivElement>>('chartEl');
  private chartInstance: echarts.ECharts | null = null;
  private resizeObserver: ResizeObserver | null = null;

  constructor() {
    afterNextRender(() => {
      this.initChart();
    });

    effect(() => {
      const s = this.spec();
      if (this.chartInstance && s.option) {
        this.chartInstance.setOption(s.option as echarts.EChartsOption, true);
      }
    });
  }

  private initChart() {
    const el = this.chartEl()?.nativeElement;
    if (!el) return;

    // Use dark theme matching the app
    this.chartInstance = echarts.init(el, 'dark', {
      renderer: 'canvas',
    });

    // Apply base theme overrides for Synaptiq brand
    const baseOption: echarts.EChartsOption = {
      backgroundColor: 'transparent',
      textStyle: {
        fontFamily: 'Inter, system-ui, sans-serif',
      },
      color: [
        '#b39ddb', '#4dd0e1', '#81c995', '#f28b82',
        '#fdd663', '#a5b4fc', '#f9a8d4', '#67e8f9',
      ],
      tooltip: {
        trigger: this.spec().chart_type === 'pie' || this.spec().chart_type === 'donut' ? 'item' : 'axis',
        backgroundColor: 'rgba(19, 19, 31, 0.95)',
        borderColor: 'rgba(255, 255, 255, 0.08)',
        textStyle: { color: '#e8e8f4', fontSize: 13 },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
    };

    this.chartInstance.setOption({
      ...baseOption,
      ...(this.spec().option as echarts.EChartsOption),
    });

    // Auto-resize
    this.resizeObserver = new ResizeObserver(() => {
      this.chartInstance?.resize();
    });
    this.resizeObserver.observe(el);
  }

  ngOnDestroy() {
    this.resizeObserver?.disconnect();
    this.chartInstance?.dispose();
  }
}
