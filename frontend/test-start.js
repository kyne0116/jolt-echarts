// 简单的启动测试脚本
import { spawn } from 'child_process'

console.log('🚀 启动前端开发服务器...')

const viteProcess = spawn('npm', ['run', 'dev'], {
  stdio: 'inherit',
  shell: true,
  cwd: process.cwd()
})

viteProcess.on('error', (error) => {
  console.error('❌ 启动失败:', error)
})

viteProcess.on('close', (code) => {
  console.log(`进程退出，代码: ${code}`)
})

// 监听中断信号
process.on('SIGINT', () => {
  console.log('\n🛑 正在停止服务器...')
  viteProcess.kill('SIGINT')
  process.exit(0)
})
