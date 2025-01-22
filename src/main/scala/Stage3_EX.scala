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

class Stage3_EX extends Module {
  val io = IO(new Bundle{
    val pipeline_vals = Input(new PipelineValuesEX)
    val data_in_MEM = Input(SInt(32.W))
    val data_in_WB = Input(SInt(32.W))
    val EX_control = Input(UInt(4.W))

    val rd = Output(UInt(5.W))
    val imm = Output(SInt(32.W)) //Temp
    val ctrl = Output(new ControlBus)
    val data_out_alu = Output(SInt(32.W))
    val data_out_reg2 = Output(SInt(32.W))
    val pc_update_bool = Output(Bool())
    val pc_update_val = Output(UInt(32.W))
  })

  // Output
  val rd = Wire(UInt(5.W))
  val imm = Wire(SInt(32.W)) // Temp
  val data_out_alu = Wire(SInt(32.W))
  val data_out_reg2 = Wire(SInt(32.W))
  val ctrl = Wire(new ControlBus)

  val data_reg = RegInit(0.S(32.W))

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
    rd := io.pipeline_vals.rd
    imm := io.pipeline_vals.imm
    data_out_alu := ALU.io.result
    data_out_reg2 := io.pipeline_vals.data2
    ctrl := io.pipeline_vals.ctrl

    counter := 0.U
  }

  // Output
  io.rd := RegNext(rd)
  io.imm := RegNext(imm) // Debugging
  io.ctrl := RegNext(ctrl)
  io.data_out_alu := RegNext(data_out_alu)
  io.data_out_reg2 := RegNext(data_out_reg2)
  io.pc_update_bool := RegNext(ALU.io.pc_update_bool)
  io.pc_update_val := RegNext(ALU.io.pc_update_val)
}
