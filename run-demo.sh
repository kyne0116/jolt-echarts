#!/bin/bash

# Jolt JSON转换演示运行脚本
# 
# 使用方法:
#   ./run-demo.sh [选项]
#
# 选项:
#   compile  - 编译项目
#   test     - 运行测试
#   run      - 运行演示
#   package  - 打包项目
#   clean    - 清理项目
#   all      - 执行完整流程（清理、编译、测试、打包、运行）

set -e  # 遇到错误时退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Java环境
check_java() {
    if ! command -v java &> /dev/null; then
        log_error "Java未安装或未在PATH中"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    log_info "Java版本: $JAVA_VERSION"
}

# 检查Maven环境
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装或未在PATH中"
        exit 1
    fi
    
    MAVEN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    log_info "Maven版本: $MAVEN_VERSION"
}

# 清理项目
clean_project() {
    log_info "清理项目..."
    mvn clean
    log_success "项目清理完成"
}

# 编译项目
compile_project() {
    log_info "编译项目..."
    mvn compile
    log_success "项目编译完成"
}

# 运行测试
run_tests() {
    log_info "运行测试..."
    mvn test
    log_success "测试运行完成"
    
    # 显示测试结果摘要
    if [ -f "target/surefire-reports/TEST-*.xml" ]; then
        log_info "测试结果摘要:"
        grep -h "tests=" target/surefire-reports/TEST-*.xml | head -1 | \
        sed 's/.*tests="\([^"]*\)".*failures="\([^"]*\)".*errors="\([^"]*\)".*/  总测试数: \1, 失败: \2, 错误: \3/'
    fi
}

# 打包项目
package_project() {
    log_info "打包项目..."
    mvn package -DskipTests
    log_success "项目打包完成"
    
    if [ -f "target/jolt-demo-1.0.0.jar" ]; then
        JAR_SIZE=$(du -h target/jolt-demo-1.0.0.jar | cut -f1)
        log_info "生成的JAR文件大小: $JAR_SIZE"
    fi
}

# 运行演示
run_demo() {
    log_info "运行Jolt转换演示..."
    
    if [ -f "target/jolt-demo-1.0.0.jar" ]; then
        java -jar target/jolt-demo-1.0.0.jar
    else
        mvn exec:java -Dexec.mainClass="com.example.jolt.demo.JoltTransformationDemo"
    fi
    
    log_success "演示运行完成"
}

# 显示帮助信息
show_help() {
    echo "Jolt JSON转换演示运行脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [选项]"
    echo ""
    echo "选项:"
    echo "  compile  - 编译项目"
    echo "  test     - 运行测试"
    echo "  run      - 运行演示"
    echo "  package  - 打包项目"
    echo "  clean    - 清理项目"
    echo "  all      - 执行完整流程（清理、编译、测试、打包、运行）"
    echo "  help     - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 all       # 执行完整流程"
    echo "  $0 run       # 仅运行演示"
    echo "  $0 test      # 仅运行测试"
}

# 主函数
main() {
    log_info "=== Jolt JSON转换演示项目 ==="
    
    # 检查环境
    check_java
    check_maven
    
    # 根据参数执行相应操作
    case "${1:-all}" in
        "clean")
            clean_project
            ;;
        "compile")
            compile_project
            ;;
        "test")
            run_tests
            ;;
        "package")
            package_project
            ;;
        "run")
            run_demo
            ;;
        "all")
            clean_project
            compile_project
            run_tests
            package_project
            run_demo
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
    
    log_success "操作完成!"
}

# 执行主函数
main "$@"
