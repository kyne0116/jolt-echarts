import { twoStageApi } from "@/api";
import type { TransformationStep } from "@/types";
import { defineStore } from "pinia";
import { computed, ref } from "vue";

export const useTransformationStore = defineStore("transformation", () => {
  // 状态
  const currentChartId = ref<string>("stacked_line_chart");
  const universalTemplate = ref<Record<string, any> | null>(null);
  const stage1Output = ref<Record<string, any> | null>(null);
  const stage2Output = ref<Record<string, any> | null>(null);
  const finalResult = ref<Record<string, any> | null>(null);

  const steps = ref<TransformationStep[]>([
    {
      id: "template",
      name: "获取通用模板",
      description: "获取带占位符的通用JSON模板",
      status: "pending",
    },
    {
      id: "stage1",
      name: "第一阶段转换",
      description: "结构转换（保持占位符）",
      status: "pending",
    },
    {
      id: "stage2",
      name: "第二阶段转换",
      description: "数据回填（替换占位符）",
      status: "pending",
    },
    {
      id: "complete",
      name: "转换完成",
      description: "生成最终ECharts配置",
      status: "pending",
    },
  ]);

  const loading = ref(false);
  const error = ref<string | null>(null);
  const executionTime = ref(0);

  // 计算属性
  const currentStep = computed(() => {
    const runningStep = steps.value.find((step) => step.status === "running");
    if (runningStep) return runningStep;

    const pendingStep = steps.value.find((step) => step.status === "pending");
    return pendingStep || steps.value[steps.value.length - 1];
  });

  const isCompleted = computed(() =>
    steps.value.every((step) => step.status === "completed")
  );

  const hasError = computed(() =>
    steps.value.some((step) => step.status === "error")
  );

  const progress = computed(() => {
    const completedSteps = steps.value.filter(
      (step) => step.status === "completed"
    ).length;
    return Math.round((completedSteps / steps.value.length) * 100);
  });

  // 方法
  const resetSteps = () => {
    steps.value.forEach((step) => {
      step.status = "pending";
      step.input = undefined;
      step.output = undefined;
      step.error = undefined;
      step.duration = undefined;
    });

    universalTemplate.value = null;
    stage1Output.value = null;
    stage2Output.value = null;
    finalResult.value = null;
    error.value = null;
    executionTime.value = 0;
  };

  const updateStepStatus = (
    stepId: string,
    status: TransformationStep["status"],
    data?: any
  ) => {
    const step = steps.value.find((s) => s.id === stepId);
    if (step) {
      step.status = status;
      if (data) {
        if (status === "error") {
          step.error = data;
        } else {
          step.output = data;
        }
      }
    }
  };

  const executeFullTransformation = async () => {
    const startTime = Date.now();
    loading.value = true;
    error.value = null;
    resetSteps();

    try {
      // 步骤1: 获取通用模板
      updateStepStatus("template", "running");
      const templateResponse = await twoStageApi.getTemplate(
        currentChartId.value
      );
      universalTemplate.value = templateResponse.template;
      updateStepStatus("template", "completed", templateResponse);

      // 步骤2: 第一阶段转换
      updateStepStatus("stage1", "running");
      const stage1Response = await twoStageApi.stage1Transform(
        currentChartId.value,
        universalTemplate.value
      );
      stage1Output.value = stage1Response.echartsStructure;
      updateStepStatus("stage1", "completed", stage1Response);

      // 步骤3: 第二阶段转换
      updateStepStatus("stage2", "running");
      const stage2Response = await twoStageApi.stage2Transform(
        currentChartId.value,
        stage1Output.value
      );
      stage2Output.value = stage2Response.finalEChartsConfig;
      updateStepStatus("stage2", "completed", stage2Response);

      // 步骤4: 完成
      updateStepStatus("complete", "running");
      finalResult.value = stage2Output.value;
      updateStepStatus("complete", "completed", finalResult.value);

      executionTime.value = Date.now() - startTime;
      console.log("✅ 完整转换流程执行成功");
    } catch (err: any) {
      const currentStepId = currentStep.value?.id || "unknown";
      updateStepStatus(currentStepId, "error", err.message);
      error.value = err.message || "转换过程中发生错误";
      console.error("❌ 转换流程执行失败:", err);
    } finally {
      loading.value = false;
    }
  };

  const executeStage1Only = async () => {
    if (!universalTemplate.value) {
      throw new Error("请先获取通用模板");
    }

    loading.value = true;
    try {
      const response = await twoStageApi.stage1Transform(
        currentChartId.value,
        universalTemplate.value
      );
      stage1Output.value = response.echartsStructure;
      return response;
    } finally {
      loading.value = false;
    }
  };

  const executeStage2Only = async () => {
    if (!stage1Output.value) {
      throw new Error("请先执行第一阶段转换");
    }

    loading.value = true;
    try {
      const response = await twoStageApi.stage2Transform(
        currentChartId.value,
        stage1Output.value
      );
      stage2Output.value = response.finalEChartsConfig;
      finalResult.value = stage2Output.value;
      return response;
    } finally {
      loading.value = false;
    }
  };

  const loadTemplate = async () => {
    loading.value = true;
    try {
      const response = await twoStageApi.getTemplate(currentChartId.value);
      universalTemplate.value = response.template;
      updateStepStatus("template", "completed", response);
      return response;
    } finally {
      loading.value = false;
    }
  };

  const setChartId = (chartId: string) => {
    currentChartId.value = chartId;
    resetSteps();
  };

  return {
    // 状态
    currentChartId,
    universalTemplate,
    stage1Output,
    stage2Output,
    finalResult,
    steps,
    loading,
    error,
    executionTime,

    // 计算属性
    currentStep,
    isCompleted,
    hasError,
    progress,

    // 方法
    resetSteps,
    updateStepStatus,
    executeFullTransformation,
    executeStage1Only,
    executeStage2Only,
    loadTemplate,
    setChartId,
  };
});
