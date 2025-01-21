import chisel3._
import chisel3.util._
import lib.ControlBus

class PipelineValuesEX extends Bundle {
  val data1 = SInt(32.W)
  val data2 = SInt(32.W)
  val imm = SInt(32.W)
  val rd = UInt(5.W)
  val ctrl = new ControlBus
}

class Stage3_EX(fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val pipeline_vals = Input(new PipelineValuesEX)
    val data_in_MEM = Input(SInt(32.W))
    val data_in_WB = Input(SInt(32.W))
    val EX_control = Input(UInt(4.W))

    val data_out_alu = Output(SInt(32.W))
    val data_out_reg2 = Output(SInt(32.W))
    val rd = Output(UInt(5.W))
    val imm = Output(SInt(32.W)) //Temp
    val ctrl = Output(new ControlBus)
    val pc_update_bool = Output(Bool())
    val pc_update_val = Output(UInt(32.W))
    val data_out = Output(SInt(32.W))
  })

  val data_reg = RegInit(0.S(32.W))

  // Pipeline registers
  //val pipeline_regs = Reg(new PipelineValuesEX)
  //pipeline_regs := io.pipeline_vals

  // forwarding switch
  val ALU_input = Module(new ALU_input)
  ALU_input.io.input := io.pipeline_vals
  ALU_input.io.data_EX := data_reg
  ALU_input.io.data_MEM := io.data_in_MEM
  ALU_input.io.data_WB := io.data_in_WB
  ALU_input.io.EX_control := io.EX_control


  // ALU
  val ALU = Module(new ALU)
  ALU.io.input := ALU_input.io.output
  data_reg := ALU.io.result

  // Output
  io.data_out := RegNext(ALU.io.result)
  io.imm := RegNext(io.pipeline_vals.imm) // Debugging
  io.data_out_alu := RegNext(ALU.io.result)
  io.data_out_reg2 := RegNext(io.pipeline_vals.data2)
  io.rd := RegNext(io.pipeline_vals.rd)
  io.ctrl := RegNext(io.pipeline_vals.ctrl)
  io.pc_update_bool := RegNext(ALU.io.pc_update_bool)
  io.pc_update_val := RegNext(ALU.io.pc_update_val)
}
