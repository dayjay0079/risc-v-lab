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
    val rd = Output(UInt(5.W))
    val imm = Output(SInt(32.W)) //Temp
    val data_out_alu = Output(SInt(32.W))
    val data_out_reg2 = Output(SInt(32.W))
    val ctrl = Output(new ControlBus)
    val pc_update_bool = Output(Bool())
    val pc_update_val = Output(UInt(32.W))
  })

  // Output
  val rd = Wire(UInt(5.W))
  val imm = Wire(SInt(32.W)) // Temp
  val data_out_alu = Wire(SInt(32.W))
  val data_out_reg2 = Wire(SInt(32.W))
  val ctrl = Wire(new ControlBus)

  // Pipeline registers
  val pipeline_regs = Reg(new PipelineValuesEX)
  pipeline_regs := io.pipeline_vals

  // ALU
  val ALU = Module(new ALU)
  ALU.io.input := pipeline_regs

  //Flushing logic
  val flush = WireDefault(0.B)
  val counter = RegInit(0.U(2.W))

  when(counter > 0.U && counter < 2.U) {
    flush := true.B
  } .otherwise {
    flush := RegNext(ALU.io.flush)
  }

  when(flush) {
    rd := 0.U
    imm := 0.S
    data_out_alu := 0.S
    data_out_reg2 := 0.S
    ctrl := ALU.io.ctrl_nop

    counter := counter + 1.U
  } .otherwise {
    rd := pipeline_regs.rd
    imm := pipeline_regs.imm
    data_out_alu := ALU.io.result
    data_out_reg2 := pipeline_regs.data2
    ctrl := pipeline_regs.ctrl

    counter := 0.U
  }

  // Output
  io.rd := rd
  io.imm := imm // Debugging
  io.data_out_alu := data_out_alu
  io.data_out_reg2 := data_out_reg2
  io.ctrl := ctrl
  io.pc_update_bool := ALU.io.pc_update_bool
  io.pc_update_val := ALU.io.pc_update_val
}
