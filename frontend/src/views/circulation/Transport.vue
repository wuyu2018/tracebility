<template>
  <section class="page-card">
    <h2 class="title">运输销售录入</h2>
    <el-form :model="form" label-position="top">
      <el-row :gutter="12">
        <el-col :md="12"><el-form-item label="batchId"><el-input-number v-model="form.batchId" :min="1" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="transportCompany"><el-input v-model="form.transportCompany" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="vehicleNumber"><el-input v-model="form.vehicleNumber" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="salesRegion"><el-input v-model="form.salesRegion" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="receiverName"><el-input v-model="form.receiverName" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="receiverContact"><el-input v-model="form.receiverContact" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="recorderName"><el-input v-model="form.recorderName" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="environmentTemperature"><el-input-number v-model="form.environmentTemperature" :step="0.1" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="productTemperature"><el-input-number v-model="form.productTemperature" :step="0.1" /></el-form-item></el-col>
        <el-col :md="12"><el-form-item label="time"><el-date-picker v-model="form.time" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item></el-col>
      </el-row>
      <el-button type="primary" @click="submit">提交运输销售</el-button>
    </el-form>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { recordTransportSale } from '../../services/api'

const form = reactive({
  batchId: 1, transportCompany: '', vehicleNumber: '', salesRegion: '', receiverName: '', receiverContact: '', recorderName: '',
  environmentTemperature: 25, productTemperature: 4, time: ''
})

async function submit() {
  await recordTransportSale(form)
  ElMessage.success('运输销售记录提交成功')
}
</script>
