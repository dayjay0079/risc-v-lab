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

    val data_out = Output(SInt(32.W))
    //val check_out = Output(Bool())
    val rd = Output(UInt(5.W))
    val ctrl = Output(new ControlBus)
    val branch_enable = Output(Bool())
    val branch_pc = Output(UInt(32.W))
  })

  // Pipeline registers
  val pipeline_regs = Reg(new PipelineValuesEX)
  pipeline_regs := io.pipeline_vals

  // ALU
  val ALU = Module(new ALU)
  ALU.io.input := pipeline_regs

  // Output
  io.data_out := ALU.io.result
  io.rd := pipeline_regs.rd
  io.ctrl := pipeline_regs.ctrl
  io.branch_enable := ALU.io.check
  io.branch_pc := (io.pipeline_vals.ctrl.pc.asSInt + io.pipeline_vals.imm).asUInt
}
